--
-- Likes:
--


create table epes.fav (
  account         text not null references epes.account (id),
  target          text not null,
  primary key (account, target)
);


create index on epes.fav (account);
create index on epes.fav (target);
