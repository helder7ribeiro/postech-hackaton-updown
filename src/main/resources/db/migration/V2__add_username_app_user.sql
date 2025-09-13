
alter table app_user
    add column username varchar(100);

update app_user
set username = split_part(email, '@', 1)
where username is null;

alter table app_user
    alter column username set not null;

alter table app_user
    add constraint uq_app_user_username unique (username);