package gg.grounds.application

import gg.grounds.domain.model.MaintenanceConfigEntity
import gg.grounds.domain.model.MaintenanceWhitelistEntity
import gg.grounds.domain.model.MotdEntity
import gg.grounds.domain.service.MaintenanceService
import gg.grounds.domain.service.MotdService
import gg.grounds.grpc.status.StatusEvent
import gg.grounds.grpc.status.StatusEventType
import gg.grounds.grpc.status.StatusSnapshot
import gg.grounds.mapper.ProtoMapper
import io.quarkus.hibernate.reactive.panache.Panache
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.tuples.Tuple3
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jboss.logging.Logger
import java.time.Instant
import java.util.function.Function
import java.util.function.Supplier

@ApplicationScoped
class SnapshotService {
    @Inject
    lateinit var motdService: MotdService

    @Inject
    lateinit var maintenanceService: MaintenanceService

    @Inject
    lateinit var mapper: ProtoMapper

    fun buildSnapshot(nowOverride: Instant?): Uni<StatusSnapshot?>? {
        val now = nowOverride ?: Instant.now()

        LOG.debugf("Building status snapshot (now=%s)", now)

        val activeMotdUni = motdService.getActive(now)
        val maintenanceUni = maintenanceService.getConfig()
        val whitelistUni = maintenanceService.listWhitelist()

        return Panache.withSession<StatusSnapshot?>(Supplier {
            Uni.combine().all().unis<MotdEntity?, MaintenanceConfigEntity, MutableList<MaintenanceWhitelistEntity?>?>(
                activeMotdUni,
                maintenanceUni,
                whitelistUni
            ).asTuple()
                .map<StatusSnapshot?>(Function { t: Tuple3<MotdEntity?, MaintenanceConfigEntity, MutableList<MaintenanceWhitelistEntity?>?>? ->
                    val motd = t!!.getItem1()
                    val cfg = t.getItem2()
                    val whitelist = t.getItem3()

                    val effective = maintenanceService.isEffectiveEnabled(cfg, now)

                    if (effective && !cfg.enabled) {
                        LOG.infof(
                            "Maintenance is effectively ENABLED due to time window (enabled=false, startsAt=%s, endsAt=%s)",
                            cfg.startsAt,
                            cfg.endsAt
                        )
                    }

                    val snap = StatusSnapshot.newBuilder()
                        .setActiveMotd(mapper.toProto(motd))
                        .setMaintenance(mapper.toProto(cfg, effective))
                        .setGeneratedAt(mapper.ts(now))
                    snap.build()
                })
        }
        )
    }

    fun buildEvent(type: StatusEventType?): Uni<StatusEvent?>? {
        val now = Instant.now()
        return buildSnapshot(now)!!
            .map<StatusEvent?>(Function { snapshot: StatusSnapshot? ->
                StatusEvent.newBuilder()
                    .setType(type)
                    .setSnapshot(snapshot)
                    .setEmittedAt(mapper.ts(now))
                    .build()
            })
    }

    companion object {
        private val LOG: Logger = Logger.getLogger(SnapshotService::class.java)
    }
}
