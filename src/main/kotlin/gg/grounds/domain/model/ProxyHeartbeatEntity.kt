package gg.grounds.domain.model

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "proxy_heartbeats", schema = "status")
class ProxyHeartbeatEntity : PanacheEntityBase() {
    @Id
    var proxyId: String? = null

    var playerCount: Int = 0
    var healthy: Boolean = false
    var version: String? = null

    @Column(columnDefinition = "jsonb")
    var metaJson: String? = null

    var lastSeenAt: Instant? = null
}
