package gg.grounds.repo

import gg.grounds.domain.model.ProxyHeartbeatEntity
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import java.time.Instant

@ApplicationScoped
class ProxyHeartbeatRepository : PanacheRepositoryBase<ProxyHeartbeatEntity?, String?> {
    fun countOffline(cutoff: Instant?): Uni<Long?>? {
        return count("lastSeenAt < ?1", cutoff)
    }
}
