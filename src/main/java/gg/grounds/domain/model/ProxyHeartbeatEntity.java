package gg.grounds.domain.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "proxy_heartbeats", schema = "status")
public class ProxyHeartbeatEntity extends PanacheEntityBase {

    @Id
    public String proxyId;

    public int playerCount;
    public boolean healthy;
    public String version;

    @Column(columnDefinition = "jsonb")
    public String metaJson;

    public Instant lastSeenAt;
}
