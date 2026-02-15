package gg.grounds.domain.service

import gg.grounds.domain.model.MaintenanceConfigEntity
import gg.grounds.domain.model.MaintenanceWhitelistEntity
import gg.grounds.repo.MaintenanceConfigRepository
import gg.grounds.repo.MaintenanceWhitelistRepository
import io.quarkus.hibernate.reactive.panache.Panache
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.time.Instant
import java.util.*
import java.util.function.Consumer
import java.util.function.Supplier

@ApplicationScoped
class MaintenanceService {
    @Inject
    lateinit var configRepository: MaintenanceConfigRepository

    @Inject
    lateinit var whitelistRepository: MaintenanceWhitelistRepository

    val config: Uni<MaintenanceConfigEntity?>?
        get() = Panache.withSession<MaintenanceConfigEntity?>(Supplier {
            configRepository.findById(1.toShort())
                .onItem().ifNull().failWith(IllegalStateException("maintenance_config row missing (id=1)"))
        }
        )

    fun isEffectiveEnabled(c: MaintenanceConfigEntity?, now: Instant): Boolean {
        if (c == null) return false
        if (c.enabled) return true
        if (c.startsAt == null || c.endsAt == null) return false
        return !now.isBefore(c.startsAt) && !now.isAfter(c.endsAt)
    }

    fun setConfig(
        enabled: Boolean,
        message: String?,
        startsAt: Instant?,
        endsAt: Instant?
    ): Uni<MaintenanceConfigEntity?>? {
        return Panache.withTransaction<MaintenanceConfigEntity?>(Supplier {
            configRepository.findById(1.toShort())
                .onItem().ifNull().failWith(IllegalStateException("maintenance_config row missing (id=1)"))
                .invoke(Consumer { cfg: MaintenanceConfigEntity? ->
                    cfg!!.enabled = enabled
                    cfg.message = message
                    cfg.startsAt = startsAt
                    cfg.endsAt = endsAt
                    cfg.updatedAt = Instant.now()
                })
        }
        )
    }

    fun addWhitelist(uuid: UUID?, note: String?): Uni<MaintenanceWhitelistEntity?> {
        return Panache.withTransaction {
            whitelistRepository.findById(uuid)
                ?.flatMap { existing ->
                    if (existing != null) {
                        existing.note = note
                        Uni.createFrom().item(existing)
                    } else {
                        val e = MaintenanceWhitelistEntity()
                        e.uuid = uuid
                        e.note = note
                        e.createdAt = Instant.now()
                        whitelistRepository.persist(e)?.replaceWith(e)
                    }
                }
        }
    }

    fun removeWhitelist(uuid: UUID?): Uni<Boolean?>? {
        return Panache.withTransaction<Boolean?>(Supplier { whitelistRepository.deleteById(uuid) })
    }

    fun listWhitelist(): Uni<MutableList<MaintenanceWhitelistEntity?>?>? {
        return Panache.withSession<MutableList<MaintenanceWhitelistEntity?>?>(Supplier { whitelistRepository.listAll() })
    }
}
