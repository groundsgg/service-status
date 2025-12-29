package gg.grounds.grpc;

import com.google.protobuf.Empty;
import gg.grounds.domain.model.MotdEntity;
import gg.grounds.domain.service.HeartbeatService;
import gg.grounds.domain.service.MaintenanceService;
import gg.grounds.domain.service.MotdService;
import gg.grounds.grpc.status.*;
import gg.grounds.infrastructure.event.StatusEventBus;
import gg.grounds.mapper.ProtoMapper;
import gg.grounds.application.*;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;

import java.time.Instant;
import java.util.UUID;

@GrpcService
public class StatusGrpcService extends MutinyStatusServiceGrpc.StatusServiceImplBase {

    @Inject
    MotdService motdService;
    @Inject
    MaintenanceService maintenanceService;
    @Inject
    HeartbeatService heartbeatService;
    @Inject
    SnapshotService snapshotService;

    @Inject
    StatusEventBus eventBus;
    @Inject
    ProtoMapper mapper;
    @Inject
    StatusChangePublisher publisher;


    // ----------------- STREAM -----------------

    @Override
    public Multi<StatusEvent> subscribeStatus(SubscribeStatusRequest request) {
        Multi<StatusEvent> initial = Multi.createFrom().uni(
                snapshotService.buildEvent(StatusEventType.STATUS_EVENT_TYPE_SNAPSHOT)
        );

        return Multi.createBy().concatenating().streams(
                initial,
                eventBus.stream()
        );
    }

    // ----------------- MOTD -----------------

    @Override
    public Uni<Motd> createMotd(CreateMotdRequest request) {
        MotdEntity e = new MotdEntity();
        e.name = request.getName();
        e.payloadJson = request.getPayloadJson();
        e.priority = request.getPriority();
        e.startsAt = mapper.fromTs(request.getStartsAt());
        e.endsAt = mapper.fromTs(request.getEndsAt());
        e.enabled = request.getEnabled();

    return motdService.create(e)
            .map(mapper::toProto)
            .invoke(publisher::motdChanged);
  }

    @Override
    public Uni<Motd> updateMotd(UpdateMotdRequest request) {
        UUID id = UUID.fromString(request.getId());

        MotdEntity e = new MotdEntity();
        e.name = request.getName();
        e.payloadJson = request.getPayloadJson();
        e.priority = request.getPriority();
        e.startsAt = mapper.fromTs(request.getStartsAt());
        e.endsAt = mapper.fromTs(request.getEndsAt());
        e.enabled = request.getEnabled();

        return motdService.update(id, e)
                .map(mapper::toProto)
                .invoke(publisher::motdChanged);
    }

    @Override
    public Uni<Motd> getMotd(GetMotdRequest request) {
        return motdService.get(UUID.fromString(request.getId()))
                .map(mapper::toProto);
    }

    @Override
    public Uni<Empty> deleteMotd(DeleteMotdRequest request) {
        return motdService.delete(UUID.fromString(request.getId()))
                .replaceWith(Empty.getDefaultInstance())
                .invoke(publisher::motdChanged);
    }

    @Override
    public Uni<ListMotdsResponse> listMotds(ListMotdsRequest request) {
        return motdService.list(request.getIncludeDisabled())
                .map(list -> {
                    ListMotdsResponse.Builder b = ListMotdsResponse.newBuilder();
                    for (var e : list) b.addMotds(mapper.toProto(e));
                    return b.build();
                });
    }

    @Override
    public Uni<GetActiveMotdResponse> getActiveMotd(GetActiveMotdRequest request) {
        Instant now = mapper.fromTs(request.getNow());
        return motdService.getActive(now)
                .map(active -> GetActiveMotdResponse.newBuilder()
                        .setMotd(mapper.toProto(active))
                        .build());
    }

    // ----------------- MAINTENANCE -----------------


    @Override
    public Uni<GetMaintenanceResponse> getMaintenance(GetMaintenanceRequest request) {
        return maintenanceService.getConfig()
                .map(cfg -> {
                    boolean effective = maintenanceService.isEffectiveEnabled(cfg, Instant.now());
                    return GetMaintenanceResponse.newBuilder()
                            .setConfig(mapper.toProto(cfg, effective))
                            .build();
                });
    }

    @Override
    public Uni<GetMaintenanceResponse> setMaintenance(SetMaintenanceRequest request) {
        Instant starts = mapper.fromTs(request.getStartsAt());
        Instant ends = mapper.fromTs(request.getEndsAt());

        return maintenanceService.setConfig(request.getEnabled(), request.getMessage(), starts, ends)
                .map(cfg -> {
                    boolean effective = maintenanceService.isEffectiveEnabled(cfg, Instant.now());
                    return GetMaintenanceResponse.newBuilder()
                            .setConfig(mapper.toProto(cfg, effective))
                            .build();
                })
                .invoke(publisher::maintenanceChanged);
    }

    @Override
    public Uni<WhitelistEntry> addWhitelist(AddWhitelistRequest request) {
        UUID uuid = UUID.fromString(request.getUuid());
        return maintenanceService.addWhitelist(uuid, request.getNote())
                .map(mapper::toProto);
    }

    @Override
    public Uni<Empty> removeWhitelist(RemoveWhitelistRequest request) {
        UUID uuid = UUID.fromString(request.getUuid());
        return maintenanceService.removeWhitelist(uuid)
                .replaceWith(Empty.getDefaultInstance());
    }

    @Override
    public Uni<ListWhitelistResponse> listWhitelist(ListWhitelistRequest request) {
        return maintenanceService.listWhitelist()
                .map(list -> {
                    ListWhitelistResponse.Builder b = ListWhitelistResponse.newBuilder();
                    for (var e : list) b.addEntries(mapper.toProto(e));
                    return b.build();
                });
    }

    // ----------------- HEARTBEAT -----------------


    @Override
    public Uni<SendHeartbeatResponse> sendHeartbeat(SendHeartbeatRequest request) {
        ProxyHeartbeat hb = request.getHeartbeat();
        String metaJson = "{}";

        if (hb.getMetaCount() > 0) {
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (var entry : hb.getMetaMap().entrySet()) {
                if (!first) sb.append(",");
                first = false;
                sb.append("\"").append(escapeJson(entry.getKey())).append("\":")
                        .append("\"").append(escapeJson(entry.getValue())).append("\"");
            }
            sb.append("}");
            metaJson = sb.toString();
        }

        return heartbeatService.upsert(
                hb.getProxyId(),
                hb.getPlayerCount(),
                hb.getHealthy(),
                hb.getVersion(),
                metaJson
        )
                .map(ignored -> SendHeartbeatResponse.newBuilder()
                        .setReceivedAt(mapper.ts(Instant.now()))
                        .build());
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
