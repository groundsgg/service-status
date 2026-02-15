package gg.grounds.domain.model

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "maintenance_config", schema = "status")
class MaintenanceConfigEntity : PanacheEntityBase() {
    @Id
    var id: Short = 0 // always 1

    var enabled: Boolean = false
    var message: String? = null

    @Column(name = "starts_at")
    var startsAt: Instant? = null
    @Column(name = "ends_at")
    var endsAt: Instant? = null

    @Column(name = "updated_at")
    var updatedAt: Instant? = null
}
