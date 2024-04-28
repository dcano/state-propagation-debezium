CREATE SCHEMA IF NOT EXISTS db_migrations;
CREATE SCHEMA IF NOT EXISTS courses_context;

create table courses_context.course_definition (
    id varchar(255) not null
    primary key (id)
);