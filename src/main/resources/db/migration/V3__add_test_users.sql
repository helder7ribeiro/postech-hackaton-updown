insert into app_user (id, email, username)
values
    (gen_random_uuid(), 'admin@frames.com', split_part('admin@frames.com', '@', 1)),
    (gen_random_uuid(), 'user1@frames.com', split_part('user1@frames.com', '@', 1)),
    (gen_random_uuid(), 'user2@frames.com', split_part('user2@frames.com', '@', 1)),
    (gen_random_uuid(), 'test@frames.com',  split_part('test@frames.com', '@', 1));
