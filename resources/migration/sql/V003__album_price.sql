--
-- album_price:
--


create or replace function epes.album_price(album_id text) returns integer
  as $$
  select 
      cast(round(sum(track.length) / 1400, 0) as int)
    from
      mb.track
    where
      track.album = album_id
  $$
  language sql;
