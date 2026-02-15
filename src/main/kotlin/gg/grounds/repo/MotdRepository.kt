package gg.grounds.repo

import gg.grounds.domain.model.MotdEntity
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import java.time.Instant
import java.util.*

@ApplicationScoped
class MotdRepository : PanacheRepositoryBase<MotdEntity?, UUID?> {
    fun findActive(now: Instant?): Uni<MotdEntity?>? {
        return find(
            "enabled = true and (startsAt is null or startsAt <= ?1) and (endsAt is null or endsAt >= ?1) " +
                    "order by priority desc, updatedAt desc",
            now
        ).firstResult<MotdEntity?>()
    }
}
