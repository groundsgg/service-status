package gg.grounds.domain.service;

import gg.grounds.domain.model.MaintenanceConfigEntity;
import gg.grounds.domain.model.MaintenanceWhitelistEntity;
import gg.grounds.repo.MaintenanceConfigRepository;
import gg.grounds.repo.MaintenanceWhitelistRepository;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class MaintenanceService {

    @Inject
    MaintenanceConfigRepository configRepository;
    @Inject
    MaintenanceWhitelistRepository whitelistRepository;

    public Uni<MaintenanceConfigEntity> getConfig() {
        return Panache.withSession(() ->
                configRepository.findById((short)1)
                        .onItem().ifNull().failWith(new IllegalStateException("maintenance_config row missing (id=1)"))
        );
    }

    public boolean isEffectiveEnabled(MaintenanceConfigEntity c, Instant now) {
        if (c == null) return false;
        if (c.enabled) return true;
        if (c.startsAt == null || c.endsAt == null) return false;
        return !now.isBefore(c.startsAt) && !now.isAfter(c.endsAt);
    }

    public Uni<MaintenanceConfigEntity> setConfig(boolean enabled, String message, Instant startsAt, Instant endsAt) {
        return Panache.withTransaction(() ->
                configRepository.findById((short)1)
                        .onItem().ifNull().failWith(new IllegalStateException("maintenance_config row missing (id=1)"))
                        .invoke(cfg -> {
                            cfg.enabled = enabled;
                            cfg.message = message;
                            cfg.startsAt = startsAt;
                            cfg.endsAt = endsAt;
                            cfg.updatedAt = Instant.now();
                        })
        );
    }

    public Uni<MaintenanceWhitelistEntity> addWhitelist(UUID uuid, String note) {
        return Panache.withTransaction(() ->
                whitelistRepository.findById(uuid)
                        .flatMap(existing -> {
                            if (existing != null) {
                                existing.note = note;
                                return Uni.createFrom().item(existing);
                            }
                            MaintenanceWhitelistEntity e = new MaintenanceWhitelistEntity();
                            e.uuid = uuid;
                            e.note = note;
                            e.createdAt = Instant.now();
                            return whitelistRepository.persist(e).replaceWith(e);
                        })
        );
    }

    public Uni<Boolean> removeWhitelist(UUID uuid) {
        return Panache.withTransaction(() -> whitelistRepository.deleteById(uuid));
    }

    public Uni<List<MaintenanceWhitelistEntity>> listWhitelist() {
        return Panache.withSession(whitelistRepository::listAll);
    }

}
