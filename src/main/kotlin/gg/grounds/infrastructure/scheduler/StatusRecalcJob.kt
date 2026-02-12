package gg.grounds.infrastructure.scheduler

import gg.grounds.application.SnapshotService
import gg.grounds.grpc.status.StatusEvent
import gg.grounds.grpc.status.StatusEventType
import gg.grounds.infrastructure.event.StatusEventBus
import io.quarkus.runtime.StartupEvent
import io.vertx.mutiny.core.Vertx
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import jakarta.inject.Inject
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger
import java.util.function.Consumer
import kotlin.math.max

@ApplicationScoped
class StatusRecalcJob {
    @ConfigProperty(name = "service-status.recalc.enabled", defaultValue = "true")
    var enabled: Boolean = false

    @ConfigProperty(name = "service-status.recalc.interval-seconds", defaultValue = "15")
    var intervalSeconds: Long = 0

    @Inject
    lateinit var snapshotService: SnapshotService

    @Inject
    lateinit var eventBus: StatusEventBus

    @Inject
    lateinit var vertx: Vertx

    fun onStart(@Observes event: StartupEvent?) {
        if (!enabled) {
            LOG.info("Status recalc job is disabled!")
            return
        }

        val periodMs = max(1, intervalSeconds) * 1000L

        LOG.infof("Starting status recalc job (interval=%ds)...", intervalSeconds)

        vertx.setPeriodic(periodMs, Consumer { id: Long? ->
            snapshotService.buildEvent(StatusEventType.STATUS_EVENT_TYPE_SNAPSHOT)!!
                .subscribe().with(
                    Consumer { event: StatusEvent? -> eventBus.publish(event) },
                    Consumer { t: Throwable? -> LOG.error("Failed to recalculate status snapshot", t) })
        }
        )
    }

    companion object {
        private val LOG: Logger = Logger.getLogger(StatusRecalcJob::class.java)
    }
}
