--
-- Genre:
--


create table mb.genre (
  genre           text not null primary key
);


--
-- Artist:
--


create table mb.artist (
  id              text not null primary key,
  name            text not null,
  iname           text generated always as (normalize(lower(name))) stored,
  disambiguation  text
);


create index on mb.artist (iname asc);


--
-- Album:
--


create table mb.album (
  id              text not null primary key,
  name            text not null,
  iname           text generated always as (normalize(lower(name))) stored,
  artist          text not null references mb.artist (id),
  released        date not null
);


create index on mb.album (iname asc);
create index on mb.album (artist);
create index on mb.album (released asc);


--
-- Track:
--


create table mb.track (
  id              text not null primary key,
  album           text not null references mb.album (id),
  artist          text not null references mb.artist (id),
  name            text not null,
  iname           text not null generated always as (normalize(lower(name))) stored,
  position        integer not null,
  length          integer not null,
  constraint uniq_album_name unique (album, name)
);


create index on mb.track (album, position asc);
create index on mb.track (artist);
create index on mb.track (iname asc);


--
-- album_genre: many-to-many relation:
--


create table mb.album_genre (
  album           text not null references mb.album (id),
  genre           text not null references mb.genre (genre),
  primary key (album, genre)
);


create index on mb.album_genre (album);
create index on mb.album_genre (genre);
