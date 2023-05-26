--
-- API-key:
--


create table epes.apikey (
  apikey          text not null primary key,
  account         integer not null references epes.account (id)
);
