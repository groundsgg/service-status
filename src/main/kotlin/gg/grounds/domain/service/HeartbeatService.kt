package gg.grounds.domain.service

import gg.grounds.domain.model.ProxyHeartbeatEntity
import gg.grounds.repo.ProxyHeartbeatRepository
import io.quarkus.hibernate.reactive.panache.Panache
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.time.Instant
import java.util.function.Function
import java.util.function.Supplier

@ApplicationScoped
class HeartbeatService {
    @Inject
    lateinit var heartbeatRepository: ProxyHeartbeatRepository

    fun upsert(
        proxyId: String,
        playerCount: Int,
        healthy: Boolean,
        version: String?,
        metaJson: String?
    ): Uni<ProxyHeartbeatEntity?>? {
        return Panache.withTransaction {
            heartbeatRepository.findById(proxyId)
                .flatMap { existing ->
                    val now = Instant.now()
                    if (existing == null) {
                        val e = ProxyHeartbeatEntity()
                        e.proxyId = proxyId
                        e.playerCount = playerCount
                        e.healthy = healthy
                        e.version = version
                        e.metaJson = metaJson ?: "{}"
                        e.lastSeenAt = now
                        heartbeatRepository.persist(e).replaceWith(e)
                    } else {
                        existing.playerCount = playerCount
                        existing.healthy = healthy
                        existing.version = version
                        if (metaJson != null) existing.metaJson = metaJson
                        existing.lastSeenAt = now
                        Uni.createFrom().item(existing)
                    }
                }
        }
    }
}
