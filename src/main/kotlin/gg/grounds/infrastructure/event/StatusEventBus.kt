package gg.grounds.infrastructure.event

import gg.grounds.grpc.status.StatusEvent
import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class StatusEventBus {
    private val processor: BroadcastProcessor<StatusEvent?> = BroadcastProcessor.create<StatusEvent?>()

    fun publish(event: StatusEvent?) {
        processor.onNext(event)
    }

    fun stream(): Multi<StatusEvent?> {
        return processor
    }
}
