--changeset klite:users
create table users(
  ${id},
  email varchar not null,
  firstName varchar not null,
  lastName varchar not null,
  passwordHash varchar,
  locale varchar not null default 'en',
  ${createdAt}
);

--changeset klite:users_email_idx
create unique index users_email_idx on users (email);
