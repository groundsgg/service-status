package gg.grounds.repo;

import gg.grounds.domain.model.ProxyHeartbeatEntity;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;

@ApplicationScoped
public class ProxyHeartbeatRepository implements PanacheRepositoryBase<ProxyHeartbeatEntity, String> {

    public Uni<Long> countOffline(Instant cutoff) {
        return count("lastSeenAt < ?1", cutoff);
    }
}
