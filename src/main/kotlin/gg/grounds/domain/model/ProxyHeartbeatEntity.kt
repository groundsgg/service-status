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
    @Column(name = "proxy_id")
    var proxyId: String? = null

    @Column(name = "player_count")
    var playerCount: Int = 0
    var healthy: Boolean = false
    var version: String? = null

    @Column(name = "meta", columnDefinition = "jsonb")
    var metaJson: String? = null

    @Column(name = "last_seen_at")
    var lastSeenAt: Instant? = null
}
