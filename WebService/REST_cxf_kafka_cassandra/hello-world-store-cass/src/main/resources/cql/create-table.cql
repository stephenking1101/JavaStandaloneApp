use example;

create table if not exists hello_world (
    user_name       text,
    timestamp       bigint,
    extension       map<text, text>,
    primary key     (user_name, timestamp)
);

