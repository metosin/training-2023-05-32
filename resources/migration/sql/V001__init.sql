--
-- Epes schema:
--


-- Make sure we have pgcrypto installed:

create extension if not exists pgcrypto schema public;


--
-- Accounts:
--


create table epes.account (
  id              serial not null primary key,
  username        text not null unique,
  password        text not null,
  fullname        text not null,
  role            text not null
);


create index on epes.account (username);

