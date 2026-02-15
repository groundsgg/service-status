package gg.grounds.domain.model

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.*

@Entity
@Table(name = "maintenance_whitelist", schema = "status")
class MaintenanceWhitelistEntity : PanacheEntityBase() {
    @Id
    var uuid: UUID? = null

    var note: String? = null
    @Column(name = "created_at")
    var createdAt: Instant? = null
}
