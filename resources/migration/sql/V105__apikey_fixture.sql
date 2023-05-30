--
-- Example API-keys:
--


insert into epes.apikey
  (apikey, account)
values
  ('tina', (select account.id from epes.account where account.username = 'tina')),
  ('james', (select account.id from epes.account where account.username = 'james'));
