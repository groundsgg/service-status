package gg.grounds.repo;

import gg.grounds.domain.model.MaintenanceWhitelistEntity;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class MaintenanceWhitelistRepository implements PanacheRepositoryBase<MaintenanceWhitelistEntity, UUID> {}
