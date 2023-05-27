--
-- API-key:
--


create table epes.apikey (
  apikey          text not null primary key,
  account         text not null references epes.account (id)
);
