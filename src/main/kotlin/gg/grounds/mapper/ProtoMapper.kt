package gg.grounds.mapper

import com.google.protobuf.Timestamp
import gg.grounds.domain.model.MaintenanceConfigEntity
import gg.grounds.domain.model.MaintenanceWhitelistEntity
import gg.grounds.domain.model.MotdEntity
import gg.grounds.grpc.status.MaintenanceConfig
import gg.grounds.grpc.status.Motd
import gg.grounds.grpc.status.WhitelistEntry
import jakarta.enterprise.context.ApplicationScoped
import java.time.Instant

@ApplicationScoped
class ProtoMapper {
    fun ts(i: Instant?): Timestamp? {
        if (i == null) return Timestamp.getDefaultInstance()
        return Timestamp.newBuilder()
            .setSeconds(i.getEpochSecond())
            .setNanos(i.getNano())
            .build()
    }

    fun fromTs(t: Timestamp?): Instant? {
        if (t == null || (t.getSeconds() == 0L && t.getNanos() == 0)) return null
        return Instant.ofEpochSecond(t.getSeconds(), t.getNanos().toLong())
    }

    fun toProto(e: MotdEntity?): Motd? {
        return if (e == null) Motd.getDefaultInstance() else Motd.newBuilder()
            .setId(e.id.toString())
            .setName(e.name)
            .setPayloadJson(e.payloadJson)
            .setPriority(e.priority)
            .setStartsAt(ts(e.startsAt))
            .setEndsAt(ts(e.endsAt))
            .setEnabled(e.enabled)
            .setCreatedAt(ts(e.createdAt))
            .setUpdatedAt(ts(e.updatedAt))
            .build()
    }

    fun toProto(e: MaintenanceConfigEntity?, effective: Boolean): MaintenanceConfig? {
        if (e == null) return MaintenanceConfig.getDefaultInstance()

        return MaintenanceConfig.newBuilder()
            .setEnabled(e.enabled)
            .setMessage(if (e.message == null) "" else e.message)
            .setStartsAt(ts(e.startsAt))
            .setEndsAt(ts(e.endsAt))
            .setUpdatedAt(ts(e.updatedAt))
            .setEnabled(effective)
            .build()
    }

    fun toProto(e: MaintenanceWhitelistEntity?): WhitelistEntry {
        return WhitelistEntry.newBuilder()
            .setUuid(e?.uuid.toString())
            .setNote(if (e?.note == null) "" else e.note)
            .setCreatedAt(ts(e?.createdAt))
            .build()
    }
}
