package gg.grounds.infrastructure;

import gg.grounds.application.SnapshotService;
import gg.grounds.application.StatusChangePublisher;
import gg.grounds.grpc.status.StatusEventType;
import gg.grounds.infrastructure.event.StatusEventBus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class StatusChangePublisherImpl implements StatusChangePublisher {

    @Inject
    SnapshotService snapshotService;
    @Inject
    StatusEventBus eventBus;

    public void motdChanged() {
        snapshotService.buildEvent(StatusEventType.STATUS_EVENT_TYPE_MOTD_CHANGED)
                .subscribe().with(eventBus::publish, t -> {});
    }

    public void maintenanceChanged() {
        snapshotService.buildEvent(StatusEventType.STATUS_EVENT_TYPE_MAINTENANCE_CHANGED)
                .subscribe().with(eventBus::publish, t -> {});
    }
}
