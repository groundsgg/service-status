package gg.grounds.domain.model

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.*

@Entity
@Table(name = "motds", schema = "status")
class MotdEntity : PanacheEntityBase() {
    @Id
    var id: UUID? = null

    @Column(nullable = false)
    var name: String? = null

    @Column(name = "payload_json", nullable = false, columnDefinition = "jsonb")
    var payloadJson: String? = null

    var priority: Int = 0

    @Column(name = "starts_at")
    var startsAt: Instant? = null
    @Column(name = "ends_at")
    var endsAt: Instant? = null

    var enabled: Boolean = false

    @Column(name = "created_at")
    var createdAt: Instant? = null
    @Column(name = "updated_at")
    var updatedAt: Instant? = null
}
