package gg.grounds.domain.service;

import gg.grounds.domain.model.MotdEntity;
import gg.grounds.repo.MotdRepository;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class MotdService {

    @Inject
    MotdRepository motdRepository;

    public Uni<MotdEntity> create(MotdEntity e) {
        return Panache.withTransaction(() -> {
            e.id = UUID.randomUUID();
            e.createdAt = Instant.now();
            e.updatedAt = e.createdAt;
            return motdRepository.persist(e).replaceWith(e);
        });
    }

    public Uni<MotdEntity> update(UUID id, MotdEntity update) {
        return Panache.withTransaction(() ->
           motdRepository.findById(id)
                   .onItem().ifNull().failWith(new IllegalArgumentException("MOTD not found: " + id))
                   .invoke(existing -> {
                       existing.name = update.name;
                       existing.payloadJson = update.payloadJson;
                       existing.priority = update.priority;
                       existing.startsAt = update.startsAt;
                       existing.endsAt = update.endsAt;
                       existing.enabled = update.enabled;
                       existing.updatedAt = Instant.now();
                   })
        );
    }

    public Uni<MotdEntity> get(UUID id) {
        return Panache.withSession(() -> motdRepository.findById(id))
                .onItem().ifNull().failWith(new IllegalArgumentException("MOTD not found: " + id));
    }

    public Uni<Boolean> delete(UUID id) {
        return Panache.withTransaction(() -> motdRepository.deleteById(id));
    }

    public Uni<List<MotdEntity>> list(boolean includeDisabled) {
        return Panache.withSession(() -> includeDisabled ? motdRepository.listAll() : motdRepository.list("enabled = true"));
    }

    public Uni<MotdEntity> getActive(Instant now) {
        Instant ts = now == null ? Instant.now() : now;
        return Panache.withSession(() -> motdRepository.findActive(ts));
    }
}
