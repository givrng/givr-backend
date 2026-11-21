alter table if exists project add column is_deleted boolean;
update project set is_deleted=false;

alter table if exists project alter column is_deleted set not null;