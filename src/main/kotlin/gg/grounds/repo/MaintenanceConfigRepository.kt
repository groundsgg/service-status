package gg.grounds.repo

import gg.grounds.domain.model.MaintenanceConfigEntity
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class MaintenanceConfigRepository : PanacheRepositoryBase<MaintenanceConfigEntity?, Short?>
