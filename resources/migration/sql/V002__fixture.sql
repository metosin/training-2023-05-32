--
-- Accounts:
--


insert into epes.account
  (fullname, username, password, role)
values
  ('Tina Turner', 'tina', crypt('tina', gen_salt('bf', 4)), 'admin'),
  ('James Hetfield', 'james', crypt('james', gen_salt('bf', 4)), 'user'),
  ('Cyndi Lauper', 'cyndi', crypt('cyndi', gen_salt('bf', 4)), 'user');


