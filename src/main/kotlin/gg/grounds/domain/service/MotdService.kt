package gg.grounds.domain.service

import gg.grounds.domain.model.MotdEntity
import gg.grounds.repo.MotdRepository
import io.quarkus.hibernate.reactive.panache.Panache
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.time.Instant
import java.util.*
import java.util.function.Consumer
import java.util.function.Supplier

@ApplicationScoped
class MotdService {
    @Inject
    lateinit var motdRepository: MotdRepository

    fun create(e: MotdEntity): Uni<MotdEntity?>? {
        return Panache.withTransaction<MotdEntity?>(Supplier {
            e.id = UUID.randomUUID()
            e.createdAt = Instant.now()
            e.updatedAt = e.createdAt
            motdRepository.persist(e).replaceWith<MotdEntity?>(e)
        })
    }

    fun update(id: UUID?, update: MotdEntity): Uni<MotdEntity?>? {
        return Panache.withTransaction<MotdEntity?>(Supplier {
            motdRepository.findById(id)
                .onItem().ifNull().failWith(IllegalArgumentException("MOTD not found: $id"))
                .invoke(Consumer { existing: MotdEntity? ->
                    existing!!.name = update.name
                    existing.payloadJson = update.payloadJson
                    existing.priority = update.priority
                    existing.startsAt = update.startsAt
                    existing.endsAt = update.endsAt
                    existing.enabled = update.enabled
                    existing.updatedAt = Instant.now()
                })
        }
        )
    }

    fun get(id: UUID?): Uni<MotdEntity?>? {
        return Panache.withSession<MotdEntity?>(Supplier { motdRepository.findById(id) })
            .onItem().ifNull().failWith(IllegalArgumentException("MOTD not found: $id"))
    }

    fun delete(id: UUID?): Uni<Boolean?>? {
        return Panache.withTransaction<Boolean?>(Supplier { motdRepository.deleteById(id) })
    }

    fun list(includeDisabled: Boolean): Uni<MutableList<MotdEntity?>?>? {
        return Panache.withSession<MutableList<MotdEntity?>?>(Supplier {
            if (includeDisabled) motdRepository.listAll() else motdRepository.list(
                "enabled = true"
            )
        })
    }

    fun getActive(now: Instant?): Uni<MotdEntity?>? {
        val ts = now ?: Instant.now()
        return Panache.withSession<MotdEntity?>(Supplier { motdRepository.findActive(ts) })
    }
}
