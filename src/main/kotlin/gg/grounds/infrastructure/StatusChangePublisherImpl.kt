package gg.grounds.infrastructure

import gg.grounds.application.SnapshotService
import gg.grounds.application.StatusChangePublisher
import gg.grounds.grpc.status.StatusEvent
import gg.grounds.grpc.status.StatusEventType
import gg.grounds.infrastructure.event.StatusEventBus
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.function.Consumer

@ApplicationScoped
class StatusChangePublisherImpl : StatusChangePublisher {
    @Inject
    lateinit var snapshotService: SnapshotService

    @Inject
    lateinit var eventBus: StatusEventBus

    override fun motdChanged() {
        snapshotService.buildEvent(StatusEventType.STATUS_EVENT_TYPE_MOTD_CHANGED)!!
            .subscribe()
            .with(Consumer { event: StatusEvent? -> eventBus.publish(event) }, Consumer { t: Throwable? -> })
    }

    override fun maintenanceChanged() {
        snapshotService.buildEvent(StatusEventType.STATUS_EVENT_TYPE_MAINTENANCE_CHANGED)!!
            .subscribe()
            .with(Consumer { event: StatusEvent? -> eventBus.publish(event) }, Consumer { t: Throwable? -> })
    }
}
