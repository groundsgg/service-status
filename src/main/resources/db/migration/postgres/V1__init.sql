create schema if not exists status;

create table if not exists status.motds (
                                            id uuid primary key,
                                            name text not null,
                                            payload_json jsonb not null,
                                            priority int not null default 0,
                                            starts_at timestamptz null,
                                            ends_at timestamptz null,
                                            enabled boolean not null default true,
                                            created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
    );

create index if not exists idx_motds_active_window
    on status.motds (enabled, starts_at, ends_at, priority, updated_at);

create table if not exists status.maintenance_config (
                                                         id smallint primary key default 1,
                                                         enabled boolean not null default false,
                                                         message text null,
                                                         starts_at timestamptz null,
                                                         ends_at timestamptz null,
                                                         updated_at timestamptz not null default now(),
    constraint maintenance_config_single_row check (id = 1)
    );

insert into status.maintenance_config (id, enabled)
values (1, false)
    on conflict (id) do nothing;

create table if not exists status.maintenance_whitelist (
                                                            uuid uuid primary key,
                                                            note text null,
                                                            created_at timestamptz not null default now()
    );

create table if not exists status.proxy_heartbeats (
                                                       proxy_id text primary key,
                                                       player_count int not null default 0,
                                                       healthy boolean not null default true,
                                                       version text null,
                                                       meta jsonb not null default '{}'::jsonb,
                                                       last_seen_at timestamptz not null default now()
    );

create index if not exists idx_proxy_heartbeats_last_seen
    on status.proxy_heartbeats (last_seen_at desc);
