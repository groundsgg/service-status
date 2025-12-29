package gg.grounds.domain.service;

import gg.grounds.domain.model.ProxyHeartbeatEntity;
import gg.grounds.repo.ProxyHeartbeatRepository;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;

@ApplicationScoped
public class HeartbeatService {

    @Inject
    ProxyHeartbeatRepository heartbeatRepository;

    public Uni<ProxyHeartbeatEntity> upsert(String proxyId, int playerCount, boolean healthy, String version, String metaJson) {
        return Panache.withTransaction(() ->
                heartbeatRepository.findById(proxyId)
                        .flatMap(existing -> {
                            Instant now = Instant.now();
                            if (existing == null) {
                                ProxyHeartbeatEntity e = new ProxyHeartbeatEntity();
                                e.proxyId = proxyId;
                                e.playerCount = playerCount;
                                e.healthy = healthy;
                                e.version = version;
                                e.metaJson = metaJson == null ? "{}" : metaJson;
                                e.lastSeenAt = now;
                                return heartbeatRepository.persist(e).replaceWith(e);
                            }
                            existing.playerCount = playerCount;
                            existing.healthy = healthy;
                            existing.version = version;
                            if (metaJson != null) existing.metaJson = metaJson;
                            existing.lastSeenAt = now;
                            return Uni.createFrom().item(existing);
                        })
        );
    }
}
