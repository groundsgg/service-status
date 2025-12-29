package gg.grounds.mapper;

import com.google.protobuf.Timestamp;
import gg.grounds.domain.model.MaintenanceConfigEntity;
import gg.grounds.domain.model.MaintenanceWhitelistEntity;
import gg.grounds.domain.model.MotdEntity;
import gg.grounds.grpc.status.MaintenanceConfig;
import gg.grounds.grpc.status.Motd;
import gg.grounds.grpc.status.WhitelistEntry;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;

@ApplicationScoped
public class ProtoMapper {

    public Timestamp ts(Instant i) {
        if (i == null) return Timestamp.getDefaultInstance();
        return Timestamp.newBuilder()
                .setSeconds(i.getEpochSecond())
                .setNanos(i.getNano())
                .build();
    }

    public Instant fromTs(Timestamp t) {
        if (t == null || (t.getSeconds() == 0 && t.getNanos() == 0)) return null;
        return Instant.ofEpochSecond(t.getSeconds(), t.getNanos());
    }

    public Motd toProto(MotdEntity e) {
        return e == null ? Motd.getDefaultInstance() :
                Motd.newBuilder()
                        .setId(e.id.toString())
                        .setName(e.name)
                        .setPayloadJson(e.payloadJson)
                        .setPriority(e.priority)
                        .setStartsAt(ts(e.startsAt))
                        .setEndsAt(ts(e.endsAt))
                        .setEnabled(e.enabled)
                        .setCreatedAt(ts(e.createdAt))
                        .setUpdatedAt(ts(e.updatedAt))
                        .build();
    }

    public MaintenanceConfig toProto(MaintenanceConfigEntity e, boolean effective) {
        if (e == null)
            return MaintenanceConfig.getDefaultInstance();

        return MaintenanceConfig.newBuilder()
                .setEnabled(e.enabled)
                .setMessage(e.message == null ? "" : e.message)
                .setStartsAt(ts(e.startsAt))
                .setEndsAt(ts(e.endsAt))
                .setUpdatedAt(ts(e.updatedAt))
                .setEnabled(effective)
                .build();
    }

    public WhitelistEntry toProto(MaintenanceWhitelistEntity e) {
        return WhitelistEntry.newBuilder()
                .setUuid(e.uuid.toString())
                .setNote(e.note == null ? "" : e.note)
                .setCreatedAt(ts(e.createdAt))
                .build();
    }
}
