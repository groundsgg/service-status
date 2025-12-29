package gg.grounds.infrastructure.event;

import gg.grounds.grpc.status.StatusEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class StatusEventBus {

    private final BroadcastProcessor<StatusEvent> processor = BroadcastProcessor.create();

    public void publish(StatusEvent event) {
        processor.onNext(event);
    }

    public Multi<StatusEvent> stream() {
        return processor;
    }
}
