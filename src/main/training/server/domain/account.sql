-- Get favorites by account ID
-- :name get-fav-by-account-id :? :*
SELECT
  fav.target
FROM
  epes.fav
WHERE
  fav.account = :account-id


-- Add favorite
-- :name add-fav :! :n
INSERT INTO epes.fav
  (account, target)
VALUES
  (:account-id, :target-id)
ON CONFLICT DO NOTHING


-- Remove favorite
-- :name remove-fav :! :n
DELETE FROM epes.fav 
WHERE
  account = :account-id 
AND 
  target = :target-id;
