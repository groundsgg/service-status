package gg.grounds.domain.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "maintenance_config", schema = "status")
public class MaintenanceConfigEntity extends PanacheEntityBase {

    @Id
    public short id; // always 1

    public boolean enabled;
    public String message;

    public Instant startsAt;
    public Instant endsAt;

    public Instant updatedAt;
}
