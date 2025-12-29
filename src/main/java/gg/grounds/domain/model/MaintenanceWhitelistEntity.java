package gg.grounds.domain.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "maintenance_whitelist", schema = "status")
public class MaintenanceWhitelistEntity extends PanacheEntityBase {

    @Id
    public UUID uuid;

    public String note;
    public Instant createdAt;
}
