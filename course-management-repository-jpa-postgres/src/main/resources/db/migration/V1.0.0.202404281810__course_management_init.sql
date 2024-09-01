CREATE SCHEMA IF NOT EXISTS db_migrations;
CREATE SCHEMA IF NOT EXISTS courses_context;

create table courses_context.course_definition (
    id varchar(255) not null,
    status varchar(255) not null,
    teacher_id varchar(255) not null,
    tenant_id varchar(255) not null,
    title varchar(255) not null,
    objective varchar(2500) not null,
    summary varchar(2500) not null,
    description text not null,
    pre_requirement varchar(2500) not null,
    publication_date timestamp not null,
    opening_date timestamp,
    expected_duration_millis int8 not null,
    number_of_classes int4 not null,
    version int8 not null,
    primary key (id)
);

create index tenant_course_id on courses_context.course_definition (tenant_id, id);
create index tenant_status on courses_context.course_definition (tenant_id, status);