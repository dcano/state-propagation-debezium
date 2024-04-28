CREATE SCHEMA IF NOT EXISTS db_migrations;
CREATE SCHEMA IF NOT EXISTS outbox_schema;

create table outbox_schema.outbox (
    uuid varchar(255) not null,
    metadata jsonb not null,
    payload jsonb not null,
    type varchar(1000) not null,
    tenant_id varchar(255),
    epoch long not null,
    partition_key varchar(1000),
    partition int4 not null,
    primary key (uuid)
);