package gg.grounds.domain.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "motds", schema = "status")
public class MotdEntity extends PanacheEntityBase {

    @Id
    public UUID id;

    @Column(nullable = false)
    public String name;

    @Column(name = "payload_json", nullable = false, columnDefinition = "jsonb")
    public String payloadJson;

    public int priority;

    public Instant startsAt;
    public Instant endsAt;

    public boolean enabled;

    public Instant createdAt;
    public Instant updatedAt;
}
