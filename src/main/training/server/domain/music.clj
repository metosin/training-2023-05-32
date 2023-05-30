(ns training.server.domain.music
  (:require [ring.util.http-response :as resp]
            [training.server.http.cache :as cache]
            [training.server.db.hug :as hugsql]
            [training.server.api.session.middleware :as session]))


(hugsql/register-domain :training.server.domain.music)


(defn get-artist-by-id [req]
  (let [artist-id (get-in req [:parameters :path :artist-id])
        etag      (str (hash artist-id))]
    (if (= (get-in req [:headers cache/value-if-none-match]) etag)
      (resp/not-modified)
      (if-let [artist (hugsql/execute-one! req ::get-artist-by-id {:artist-id artist-id})]
        (-> artist
            (assoc :artist/albums (hugsql/execute! req ::get-albums-by-artist-id {:artist-id artist-id}))
            (resp/ok)
            (update :headers assoc cache/header-etag etag))
        (resp/not-found {:message   "can't find artist"
                         :artist-id artist-id})))))


(defn get-artists-by-name [req]
  (let [query       (get-in req [:parameters :query]) 
        artist-name (:name query)
        limit       (:limit query)
        etag        (str (hash [artist-name limit]))]
    (if (= (get-in req [:headers cache/value-if-none-match]) etag)
      (resp/not-modified)
      (-> (hugsql/execute! req ::get-artists-by-name {:artist-name artist-name
                                                      :limit       limit})
          (resp/ok)
          (update :headers assoc cache/header-etag etag)))))


(defn get-albums-by-name [req]
  (let [query      (get-in req [:parameters :query])
        album-name (:name query)
        limit      (:limit query)
        etag       (str (hash album-name))]
    (if (= (get-in req [:headers cache/value-if-none-match]) etag)
      (resp/not-modified)
      (-> (hugsql/execute! req ::get-albums-by-name {:album-name album-name
                                                     :limit      limit})
          (resp/ok)
          (update :headers assoc cache/header-etag etag)))))


(defn get-album-by-id [req]
  (let [album-id (get-in req [:parameters :path :album-id])
        etag     (str (hash album-id))]
    (if (= (get-in req [:headers cache/value-if-none-match]) etag)
      (resp/not-modified)
      (if-let [album (hugsql/execute-one! req ::get-album-by-id {:album-id album-id})]
        (-> album
            (assoc :album/tracks (hugsql/execute! req ::get-tracks-by-album-id {:album-id album-id}))
            (resp/ok)
            (update :headers assoc cache/header-etag etag))
        (resp/not-found {:message  "can't find album"
                         :album-id album-id})))))


(def routes
  ["/music" {:middleware [session/require-session-middleware]}
   ["/artist"
    ["" {:get {:parameters {:query [:map
                                    [:name {:optional true} :string]
                                    [:limit {:default 30} :int]]}
               :handler    #'get-artists-by-name}}]
    ["/:artist-id" {:get {:parameters {:path [:map [:artist-id :string]]}
                          :handler    #'get-artist-by-id}}]]
   ["/album"
    ["" {:get {:parameters {:query [:map
                                    [:name {:optional true} :string]
                                    [:limit {:default 30} :int]]}
               :handler    #'get-albums-by-name}}]
    ["/:album-id" {:get {:parameters {:path [:map [:album-id :string]]}
                         :handler    #'get-album-by-id}}]]])
