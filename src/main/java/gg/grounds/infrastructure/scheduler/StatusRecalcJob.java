package gg.grounds.infrastructure.scheduler;

import gg.grounds.grpc.status.StatusEventType;
import gg.grounds.application.SnapshotService;
import gg.grounds.infrastructure.event.StatusEventBus;
import io.quarkus.runtime.StartupEvent;
import io.vertx.mutiny.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class StatusRecalcJob {

    private static final Logger LOG = Logger.getLogger(StatusRecalcJob.class);

    @ConfigProperty(name = "service-status.recalc.enabled", defaultValue = "true")
    boolean enabled;

    @ConfigProperty(name = "service-status.recalc.interval-seconds", defaultValue = "15")
    long intervalSeconds;

    @Inject
    SnapshotService snapshotService;
    @Inject
    StatusEventBus eventBus;
    @Inject
    Vertx vertx;

    void onStart(@Observes StartupEvent event) {
        if (!enabled) {
            LOG.info("Status recalc job is disabled!");
            return;
        }

        long periodMs = Math.max(1, intervalSeconds) * 1000L;

        LOG.infof("Starting status recalc job (interval=%ds)...", intervalSeconds);

        vertx.setPeriodic(periodMs, id ->
                snapshotService.buildEvent(StatusEventType.STATUS_EVENT_TYPE_SNAPSHOT)
                        .subscribe().with(eventBus::publish, t -> LOG.error("Failed to recalculate status snapshot", t))
        );
    }
}
