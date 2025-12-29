# status-service

The **status-service** is a central gRPC-based service used to manage and synchronize
**MOTD**, **maintenance state**, and **proxy status** across all Velocity proxies in the network.

It provides a **single source of truth** for network-wide status information without
requiring proxy restarts or manual configuration.

---

## Responsibilities

The service is responsible for:

- Managing server list MOTDs (create, update, delete, time-based activation)
- Managing network-wide maintenance mode
- Maintaining a whitelist for maintenance access
- Tracking live proxy heartbeats and derived proxy status
- Broadcasting real-time status updates to all proxies

All state changes are propagated via **gRPC server streaming** to ensure consistency
across all connected proxies.

---

## Architecture Overview

```
┌─────────────────────┐
│        Proxy        │
│                     │
│  - SubscribeStatus  │◀───────────────┐
│  - SendHeartbeat    │                │
└─────────────────────┘                │
                                       │ gRPC
┌─────────────────────┐                │
│   status-service    │────────────────┘
│                     │
│  - MOTD management  │
│  - Maintenance      │
│  - Whitelist        │
│  - Proxy heartbeats │
│  - Status snapshot  │
└─────────────────────┘
            │
            │
┌─────────────────────┐
│      PostgreSQL     │
│                     │
│  - motds            │
│  - maintenance      │
│  - whitelist        │
│  - proxy_heartbeats │
└─────────────────────┘
```

---

## gRPC API

The service exposes a single gRPC service:

- **StatusService**

Key RPCs include:

- `SubscribeStatus` – server-streaming of live status updates
- `SendHeartbeat` – proxy heartbeat reporting
- `CreateMotd`, `UpdateMotd`, `DeleteMotd`
- `GetMaintenance`, `SetMaintenance`
- `AddWhitelist`, `RemoveWhitelist`, `ListWhitelist`

All gRPC contracts are defined in the **grpc-contracts-status** module.

---

## Status Synchronization Model

- Proxies subscribe once via `SubscribeStatus`
- On subscription, an initial **full status snapshot** is sent
- On changes, incremental status events are pushed automatically

This ensures:

- No polling
- No proxy restarts
- Consistent state across the network

---

## Maintenance Mode

Maintenance mode can be enabled:

- explicitly (`enabled = true`)
- or implicitly via a configured time window

The effective maintenance state is computed server-side and distributed to all proxies.
During maintenance, only whitelisted UUIDs are allowed to join.

---

## Proxy Heartbeats

Each proxy periodically sends heartbeats containing:

- proxy identifier
- player count
- health state
- optional metadata

The service derives **online/offline status** based on the last received heartbeat
and a configurable timeout.

---

## Development

Run in dev mode with live reload:

```bash
./gradlew --console=plain quarkusDev
```

Run in dev mode with live reload using DevSpace in a Kubernetes cluster:

```bash
devspace use namespace api
devspace dev
```