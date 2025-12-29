package gg.grounds.application;

import gg.grounds.domain.model.MaintenanceConfigEntity;
import gg.grounds.domain.model.MaintenanceWhitelistEntity;
import gg.grounds.domain.model.MotdEntity;
import gg.grounds.domain.service.MaintenanceService;
import gg.grounds.domain.service.MotdService;
import gg.grounds.grpc.status.StatusEvent;
import gg.grounds.grpc.status.StatusEventType;
import gg.grounds.grpc.status.StatusSnapshot;
import gg.grounds.mapper.ProtoMapper;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.List;

@ApplicationScoped
public class SnapshotService {

    private static final Logger LOG = Logger.getLogger(SnapshotService.class);

    @Inject
    MotdService motdService;
    @Inject
    MaintenanceService maintenanceService;
    @Inject
    ProtoMapper mapper;

    public Uni<StatusSnapshot> buildSnapshot(Instant nowOverride) {
        Instant now = nowOverride == null ? Instant.now() : nowOverride;

        LOG.debugf("Building status snapshot (now=%s)", now);

        Uni<MotdEntity> activeMotdUni = motdService.getActive(now);
        Uni<MaintenanceConfigEntity> maintenanceUni = maintenanceService.getConfig();
        Uni<List<MaintenanceWhitelistEntity>> whitelistUni = maintenanceService.listWhitelist();

        return Panache.withSession(() ->
                Uni.combine().all().unis(activeMotdUni, maintenanceUni, whitelistUni).asTuple()
                        .map(t -> {
                            MotdEntity motd = t.getItem1();
                            MaintenanceConfigEntity cfg = t.getItem2();
                            List<MaintenanceWhitelistEntity> whitelist = t.getItem3();

                            boolean effective = maintenanceService.isEffectiveEnabled(cfg, now);

                            if (effective && !cfg.enabled) {
                                LOG.infof(
                                        "Maintenance is effectively ENABLED due to time window (enabled=false, startsAt=%s, endsAt=%s)",
                                        cfg.startsAt,
                                        cfg.endsAt
                                );
                            }

                            StatusSnapshot.Builder snap = StatusSnapshot.newBuilder()
                                    .setActiveMotd(mapper.toProto(motd))
                                    .setMaintenance(mapper.toProto(cfg, effective))
                                    .setGeneratedAt(mapper.ts(now));

                            //TODO: Change this when contract version 0.0.3 was published.

                            /*for (var e : whitelist) {
                                snap.addWhitelist(e.uuid);
                            }*/

                            return snap.build();
                        })
        );
    }

    public Uni<StatusEvent> buildEvent(StatusEventType type) {
        Instant now = Instant.now();
        return buildSnapshot(now)
                .map(snapshot -> StatusEvent.newBuilder()
                        .setType(type)
                        .setSnapshot(snapshot)
                        .setEmittedAt(mapper.ts(now))
                        .build());
    }
}
