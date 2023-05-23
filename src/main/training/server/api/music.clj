(ns training.server.api.music
  (:require [clojure.string :as str]
            [ring.util.http-response :as resp]
            [training.server.db.honey :as sql]))


(defn get-artist-by-id [ctx artist-id]
  (sql/execute-one! ctx {:select [:artist/id :artist/name :artist/disambiguation]
                         :from   [:artist]
                         :where  [:= :artist/id artist-id]}))


(defn find-artists-by-name [ctx artist-name]
  (sql/execute! ctx {:select   [:artist/id :artist/name :artist/disambiguation
                                [[:count :album/id] :albums]]
                     :from     [:artist]
                     :join     [:album [:= :album/artist :artist/id]]
                     :group-by [:artist/id :artist/name :artist/disambiguation]
                     :where    [[:starts_with :artist/iname (str/lower-case artist-name)]]
                     :order-by [[:artist/iname :asc]]
                     :limit    100}))


(defn get-album-by-id [ctx album-id]
  (sql/execute-one! ctx {:select [:album/id :album/name :album/released
                                  :artist/id :artist/name]
                         :from   [:album]
                         :join   [:artist [:= :artist/id :album/artist]]
                         :where  [:= :album/id album-id]}))


(defn get-albums-by-artist-id [ctx artist-id]
  (sql/execute! ctx {:select   [[:album/id]
                                [:album/name]
                                [:album/released]
                                [[:count :track/id] :tracks]
                                [[:sum :track/length] :length]]
                     :from     [:album]
                     :join     [:track [:= :track/album :album/id]]
                     :where    [:= :album/artist artist-id]
                     :group-by [:album/id :album/name :album/released]
                     :order-by [[:album/released :asc]]}))

(comment
  (require '[honey.sql :as honey])
  (honey/format {:select   [[:album/id]
                            [:album/name]
                            [:album/released]
                            [[:count :track/id] :tracks]
                            [[:sum :track/length] :length]]
                 :from     [:album]
                 :join     [:track [:= :track/album :album/id]]
                 :where    [:= :album/artist "artist-id"]
                 :group-by [:album/id :album/name :album/released]
                 :order-by [[:album/released :asc]]})

  (let [ctx       {:system {:ds (:ds user/system)}}
        artist-id "d87e52c5-bb8d-4da8-b941-9f4928627dc8"]
    (get-albums-by-artist-id ctx artist-id))
  ;
  )

(defn find-albums-by-name [ctx album-name]
  (sql/execute! ctx {:select   [:album/id :album/name :album/released
                                :artist/id :artist/name]
                     :from     [:album]
                     :where    [[:starts_with :album/iname (str/lower-case album-name)]]
                     :order-by [[:album/released :asc]]}))


(defn get-tracks-by-album-id [ctx album-id]
  (sql/execute! ctx {:select   [:track/id :track/name :track/position :track/length]
                     :from     [:track]
                     :where    [:= :track/album album-id]
                     :order-by [[:track/position :asc]]}))


(def routes
  [""
   ["/artist"
    ["" {:get {:parameters {:query [:map
                                    [:name {:optional true} :string]]}
               :handler    (fn [req]
                             (let [search-name (get-in req [:parameters :query :name])
                                   etag        (get-in req [:headers "if-none-match"])]
                               (if (and (not (str/blank? etag))
                                        (= etag search-name))
                                 (resp/not-modified)
                                 (-> (find-artists-by-name req search-name)
                                     (resp/ok)
                                     (update :headers assoc "etag" search-name)))))}}]
    ["/:artist-id" {:get {:parameters {:path [:map [:artist-id :string]]}
                          :handler    (fn [req]
                                        (let [artist-id (get-in req [:parameters :path :artist-id])
                                              etag      (get-in req [:headers "if-none-match"])]
                                          (if (and (not (str/blank? etag))
                                                   (= etag artist-id))
                                            (resp/not-modified)
                                            (if-let [artist (get-artist-by-id req artist-id)]
                                              (-> artist
                                                  (assoc :artist/albums (get-albums-by-artist-id req artist-id))
                                                  (resp/ok)
                                                  (update :headers assoc "etag" artist-id))
                                              (resp/not-found {:message   "can't find artist"
                                                               :artist-id artist-id})))))}}]]
   ["/album"
    ["" {:get {:parameters {:query [:map
                                    [:name {:optional true} :string]]}
               :handler    (fn [req]
                             (let [search-name (get-in req [:parameters :query :name])
                                   etag        (get-in req [:headers "if-none-match"])]
                               (if (and (not (str/blank? etag))
                                        (= etag search-name))
                                 (resp/not-modified)
                                 (-> (find-albums-by-name req search-name)
                                     (resp/ok)
                                     (update :headers assoc "etag" search-name)))))}}]
    ["/:album-id" {:get {:parameters {:path [:map [:album-id :string]]}
                         :handler    (fn [req]
                                       (let [album-id (get-in req [:parameters :path :album-id])
                                             etag     (get-in req [:headers "if-none-match"])]
                                         (if (and (not (str/blank? etag))
                                                  (= etag album-id))
                                           (resp/not-modified)
                                           (if-let [album (get-album-by-id req album-id)]
                                             (-> album
                                                 (assoc :album/tracks (get-tracks-by-album-id req album-id))
                                                 (resp/ok)
                                                 (update :headers assoc "etag" album-id))
                                             (resp/not-found {:message  "can't find album"
                                                              :album-id album-id})))))}}]]])

