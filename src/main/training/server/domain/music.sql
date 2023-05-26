-- Find artist by artist ID
-- :name get-artist-by-id :? :1
-- :doc Get character by id
SELECT 
  artist.id, 
  artist.name, 
  artist.disambiguation 
FROM
  mb.artist 
WHERE 
  artist.id = :artist-id;
  


-- Search artist by name
-- :name get-artists-by-name :? :*
SELECT 
  artist.id, 
  artist.name, 
  artist.disambiguation, 
  COUNT(album.id) AS albums 
FROM 
  mb.artist 
INNER JOIN 
  mb.album ON album.artist = artist.id
WHERE 
  STARTS_WITH(artist.iname, :artist-name)
GROUP BY 
  artist.id, 
  artist.name, 
  artist.disambiguation 
ORDER BY 
  artist.iname ASC
LIMIT :limit


-- Get album by ID;
-- :name get-album-by-id :? :1
SELECT 
  album.id, 
  album.name, 
  album.released, 
  artist.id, 
  artist.name, 
  COUNT(track.id) AS tracks, 
  SUM(track.length) AS length, 
  EPES.ALBUM_PRICE(album.id) AS price 
FROM 
  mb.album 
LEFT JOIN 
  mb.artist ON artist.id = album.artist 
LEFT JOIN
  mb.track ON track.album = album.id 
WHERE
  album.id = :album-id
GROUP BY 
  album.id,
  album.name,
  album.released, 
  artist.id, 
  artist.name


-- Get albums by artist ID
-- :name get-albums-by-artist-id :? :*
SELECT 
  album.id, 
  album.name, 
  album.released, 
  COUNT(track.id) AS tracks, 
  SUM(track.length) AS length, 
  EPES.ALBUM_PRICE(album.id) AS price 
FROM 
  mb.album 
INNER JOIN 
  mb.track ON track.album = album.id 
WHERE 
  album.artist = :artist-id
GROUP BY 
  album.id, 
  album.name, 
  album.released 
ORDER BY 
  album.released ASC


-- Search albums by name
-- :name get-albums-by-name :? :*
SELECT 
  album.id, 
  album.name, 
  album.released, 
  artist.id, 
  artist.name 
FROM 
  mb.album 
INNER JOIN 
  mb.artist ON album.artist = artist.id
WHERE
  STARTS_WITH(album.iname, :album-name)
ORDER BY 
  album.released ASC
LIMIT :limit


-- Get album tracks
-- :name get-tracks-by-album-id :? :*
SELECT
  track.id, 
  track.name, 
  track.position, 
  track.length 
FROM 
  mb.track 
WHERE 
  track.album = :album-id
ORDER BY 
  track.position ASC
