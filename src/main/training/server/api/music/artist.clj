(ns training.server.api.music.artist
  (:require [next.jdbc :as jdbc]
            [ring.util.http-response :as resp]
            [training.server.db.honey :as sql]))


(defn get-artist-by-id [ctx artist-id]
  (let [artist (sql/execute-one! ctx {:select [:artist/id :artist/name :artist/disambiguation]
                                      :from   [:artist]
                                      :where  [:= :artist/id artist-id]})]
    (if artist
      (resp/ok artist)
      (resp/not-found {:message   "Artist not found"
                       :artist/id artist-id}))))


(defn find-artists-by-name [ctx artist-name]
  (sql/execute! ctx {:select [:artist/id :artist/name :artist/disambiguation]
                     :from   [:artist]
                     :where  [[:starts_with :artist/iname artist-name]]}))


(comment

  (require '[honey.sql :as honey])

  (def ctx {:tx (:ds user/system)})


  (find-artists-by-name ctx "pink")
  (get-artist-by-id ctx "83d91898-7763-47d7-b03b-b92132375c47")
  (let [query {:select [:artist/id :artist/name :artist/disambiguation]
               :from   [:artist]
               :where  [[:starts_with :artist/iname artist-name]]}]
    (sql/execute-one! (:ds user/system) query))


  (let [query {:select   [:album/name :album/released]
               :from     [:album]
               :where    [:= :album/artist {:select :artist/id
                                            :from   [:artist]
                                            :where  [:= :artist/iname "pink floyd"]}]
               :order-by [[:album/released :asc]]}]
    #_(sql/format query)
    (jdbc/execute! (:ds user/system) (sql/format query)))
  ;; [#:album{:name     "Atom Heart Mother"
  ;;          :released #inst "1970-10-09T22:00:00.000-00:00"}
  ;;  #:album{:name     "Relics"
  ;;          :released #inst "1971-05-13T22:00:00.000-00:00"}
  ;;  #:album{:name     "Meddle"
  ;;          :released #inst "1971-10-29T22:00:00.000-00:00"}
  ;;  #:album{:name     "Omayyad"
  ;;          :released #inst "1971-12-31T22:00:00.000-00:00"}
  ;;  #:album{:name     "Obscured by Clouds"
  ;;          :released #inst "1972-06-02T22:00:00.000-00:00"}
  ;;  #:album{:name     "The Dark Side of the Moon"
  ;;          :released #inst "1973-03-23T22:00:00.000-00:00"}
  ;;  #:album{:name     "A Nice Pair"
  ;;          :released #inst "1973-12-04T22:00:00.000-00:00"}
  ;;  #:album{:name     "Nocturnal Submission: Robot Love"
  ;;          :released #inst "1973-12-31T22:00:00.000-00:00"}
  ;;  #:album{:name     "The Screaming Abdab"
  ;;          :released #inst "1974-12-31T22:00:00.000-00:00"}
  ;;  #:album{:name     "KQED"
  ;;          :released #inst "1974-12-31T22:00:00.000-00:00"}
  ;;  #:album{:name     "Wish You Were Here"
  ;;          :released #inst "1975-09-11T22:00:00.000-00:00"}
  ;;  #:album{:name     "1970-07-16: Libest Spacement Monitor: BBC Paris Cinema, London, UK"
  ;;          :released #inst "1975-12-31T22:00:00.000-00:00"}
  ;;  #:album{:name     "Animals"
  ;;          :released #inst "1977-01-22T22:00:00.000-00:00"}
  ;;  #:album{:name     "Barrett's Revenge"
  ;;          :released #inst "1977-12-31T22:00:00.000-00:00"}
  ;;  #:album{:name     "The Wall"
  ;;          :released #inst "1979-11-29T22:00:00.000-00:00"}]



  (sql/format {:select [:name :iname :disambiquation]
               :from   :artists})

  (def ds (:ds user/system))
  (-> (jdbc/execute-one! ds (sql/format {:select [[:%now]]}))
      :now)


  ; 
  )