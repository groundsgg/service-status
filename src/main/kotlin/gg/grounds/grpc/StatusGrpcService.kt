package gg.grounds.grpc

import com.google.protobuf.Empty
import gg.grounds.application.SnapshotService
import gg.grounds.application.StatusChangePublisher
import gg.grounds.domain.model.MaintenanceConfigEntity
import gg.grounds.domain.model.MaintenanceWhitelistEntity
import gg.grounds.domain.model.MotdEntity
import gg.grounds.domain.model.ProxyHeartbeatEntity
import gg.grounds.domain.service.HeartbeatService
import gg.grounds.domain.service.MaintenanceService
import gg.grounds.domain.service.MotdService
import gg.grounds.grpc.status.*
import gg.grounds.infrastructure.event.StatusEventBus
import gg.grounds.mapper.ProtoMapper
import io.quarkus.grpc.GrpcService
import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.Uni
import jakarta.inject.Inject
import java.time.Instant
import java.util.*
import java.util.function.Function

@GrpcService
class StatusGrpcService : MutinyStatusServiceGrpc.StatusServiceImplBase() {
    @Inject
    lateinit var motdService: MotdService

    @Inject
    lateinit var maintenanceService: MaintenanceService

    @Inject
    lateinit var heartbeatService: HeartbeatService

    @Inject
    lateinit var snapshotService: SnapshotService

    @Inject
    lateinit var eventBus: StatusEventBus

    @Inject
    lateinit var mapper: ProtoMapper

    @Inject
    lateinit var publisher: StatusChangePublisher


    // ----------------- STREAM -----------------
    override fun subscribeStatus(request: SubscribeStatusRequest?): Multi<StatusEvent?>? {
        val initial = Multi.createFrom().uni<StatusEvent?>(
            snapshotService.buildEvent(StatusEventType.STATUS_EVENT_TYPE_SNAPSHOT)
        )

        return Multi.createBy().concatenating().streams<StatusEvent?>(
            initial,
            eventBus.stream()
        )
    }

    // ----------------- MOTD -----------------
    override fun createMotd(request: CreateMotdRequest): Uni<Motd?>? {
        val e = MotdEntity()
        e.name = request.getName()
        e.payloadJson = request.getPayloadJson()
        e.priority = request.getPriority()
        e.startsAt = mapper.fromTs(request.getStartsAt())
        e.endsAt = mapper.fromTs(request.getEndsAt())
        e.enabled = request.getEnabled()

        return motdService.create(e)!!
            .map<Motd?>(Function { e: MotdEntity? -> mapper.toProto(e) })
            .invoke(Runnable { publisher.motdChanged() })
    }

    override fun updateMotd(request: UpdateMotdRequest): Uni<Motd?>? {
        val id = UUID.fromString(request.getId())

        val e = MotdEntity()
        e.name = request.getName()
        e.payloadJson = request.getPayloadJson()
        e.priority = request.getPriority()
        e.startsAt = mapper.fromTs(request.getStartsAt())
        e.endsAt = mapper.fromTs(request.getEndsAt())
        e.enabled = request.getEnabled()

        return motdService.update(id, e)!!
            .map<Motd?>(Function { e: MotdEntity? -> mapper.toProto(e) })
            .invoke(Runnable { publisher.motdChanged() })
    }

    override fun getMotd(request: GetMotdRequest): Uni<Motd?>? {
        return motdService.get(UUID.fromString(request.getId()))!!
            .map<Motd?>(Function { e: MotdEntity? -> mapper.toProto(e) })
    }

    override fun deleteMotd(request: DeleteMotdRequest): Uni<Empty?>? {
        return motdService.delete(UUID.fromString(request.getId()))!!
            .replaceWith<Empty?>(Empty.getDefaultInstance())
            .invoke(Runnable { publisher.motdChanged() })
    }

    override fun listMotds(request: ListMotdsRequest): Uni<ListMotdsResponse?>? {
        return motdService.list(request.getIncludeDisabled())!!
            .map<ListMotdsResponse?>(Function { list: MutableList<MotdEntity?>? ->
                val b = ListMotdsResponse.newBuilder()
                for (e in list!!) b.addMotds(mapper.toProto(e))
                b.build()
            })
    }

    override fun getActiveMotd(request: GetActiveMotdRequest): Uni<GetActiveMotdResponse?>? {
        val now = mapper.fromTs(request.getNow())
        return motdService.getActive(now)!!
            .map<GetActiveMotdResponse?>(Function { active: MotdEntity? ->
                GetActiveMotdResponse.newBuilder()
                    .setMotd(mapper.toProto(active))
                    .build()
            })
    }


    // ----------------- MAINTENANCE -----------------
    override fun getMaintenance(request: GetMaintenanceRequest?): Uni<GetMaintenanceResponse?>? {
        return maintenanceService.config!!
            .map<GetMaintenanceResponse?>(Function { cfg: MaintenanceConfigEntity? ->
                val effective = maintenanceService.isEffectiveEnabled(cfg, Instant.now())
                GetMaintenanceResponse.newBuilder()
                    .setConfig(mapper.toProto(cfg, effective))
                    .build()
            })
    }

    override fun setMaintenance(request: SetMaintenanceRequest): Uni<GetMaintenanceResponse?>? {
        val starts = mapper.fromTs(request.getStartsAt())
        val ends = mapper.fromTs(request.getEndsAt())

        return maintenanceService.setConfig(request.getEnabled(), request.getMessage(), starts, ends)!!
            .map<GetMaintenanceResponse?>(Function { cfg: MaintenanceConfigEntity? ->
                val effective = maintenanceService.isEffectiveEnabled(cfg, Instant.now())
                GetMaintenanceResponse.newBuilder()
                    .setConfig(mapper.toProto(cfg, effective))
                    .build()
            })
            .invoke(Runnable { publisher.maintenanceChanged() })
    }

    override fun addWhitelist(request: AddWhitelistRequest): Uni<WhitelistEntry?>? {
        val uuid = UUID.fromString(request.getUuid())
        return maintenanceService.addWhitelist(uuid, request.getNote())
            .map<WhitelistEntry?>(Function { e: MaintenanceWhitelistEntity -> mapper.toProto(e) })
    }

    override fun removeWhitelist(request: RemoveWhitelistRequest): Uni<Empty?>? {
        val uuid = UUID.fromString(request.getUuid())
        return maintenanceService.removeWhitelist(uuid)!!
            .replaceWith<Empty?>(Empty.getDefaultInstance())
    }

    override fun listWhitelist(request: ListWhitelistRequest?): Uni<ListWhitelistResponse?>? {
        return maintenanceService.listWhitelist()!!
            .map<ListWhitelistResponse?>(Function { list: MutableList<MaintenanceWhitelistEntity?>? ->
                val b = ListWhitelistResponse.newBuilder()
                for (e in list!!) b.addEntries(mapper.toProto(e))
                b.build()
            })
    }


    // ----------------- HEARTBEAT -----------------
    override fun sendHeartbeat(request: SendHeartbeatRequest): Uni<SendHeartbeatResponse?>? {
        val hb = request.getHeartbeat()
        var metaJson = "{}"

        if (hb.getMetaCount() > 0) {
            val sb = StringBuilder("{")
            var first = true
            for (entry in hb.getMetaMap().entries) {
                if (!first) sb.append(",")
                first = false
                sb.append("\"").append(escapeJson(entry.key)).append("\":")
                    .append("\"").append(escapeJson(entry.value)).append("\"")
            }
            sb.append("}")
            metaJson = sb.toString()
        }

        return heartbeatService.upsert(
            hb.getProxyId(),
            hb.getPlayerCount(),
            hb.getHealthy(),
            hb.getVersion(),
            metaJson
        )!!
            .map<SendHeartbeatResponse?>(Function { ignored: ProxyHeartbeatEntity? ->
                SendHeartbeatResponse.newBuilder()
                    .setReceivedAt(mapper.ts(Instant.now()))
                    .build()
            })
    }

    companion object {
        private fun escapeJson(s: String?): String {
            if (s == null) return ""
            return s.replace("\\", "\\\\").replace("\"", "\\\"")
        }
    }
}
