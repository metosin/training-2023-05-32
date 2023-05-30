(ns training.web.view.artist-page
  (:require [helix.core :as hx :refer [$ defnc <>]]
            [helix.dom :as d]
            [reitit.frontend.easy :as rfe]
            [training.web.util :refer [icon]] 
            [training.web.use-resource :as resource :refer [use-resource]]
            [training.web.view.artist-page.utils :as u]))


(defnc ArtistList [{:keys [artists]}]
  (d/table
   {:role "grid"}
   (d/thead
    (d/tr
     (d/th "Artist")
     (d/th "Info")
     (d/th "Albums")))
   (d/tbody
    (for [artist artists
          :let   [{:artist/keys [id name disambiguation]} artist]]
      (d/tr {:key      id
             :class    "cursor-pointer"
             :on-click (fn [_] (rfe/push-state :artist {:id id}))}
            (d/td name)
            (d/td disambiguation)
            (d/td (:albums artist)))))))


(defnc ArtistsPage [match]
  (let [search  (-> match :route :parameters :query :search) 
        artists (use-resource ::artists "/api/music/artist" {:query {:name search}})]
    (d/div
     (d/input {:type        "text"
               :value       search
               :placeholder "Artist name"
               :auto-focus  true
               :on-change   (fn [^js e]
                              (let [search-value (-> e .-target .-value)]
                                (rfe/replace-state :artists nil {:search search-value})))})
     (case (:status artists)
       :pending (d/div "Loading...")
       :error (d/div "Error!")
       (:ok :refreshing) ($ ArtistList {:artists (:body artists)})))))


(defnc AlbumCard [{:keys [album fav? set-fav]}]
  (let [{:album/keys [id name released]} album
        {:keys [tracks length price]}    album
        on-click (fn [e]
                   (.preventDefault e)
                   (.stopPropagation e)
                        ;; TODO: Navigate to album
                   #_(rfe/push-state :artist {:id id}))
        on-click-fav (fn [e]
                       (.preventDefault e)
                       (.stopPropagation e)
                       (set-fav id (not fav?)))]
    (d/div {:class    ["cursor-pointer" "flex-direction-column" "flex-align-items-stretch"]
            :on-click on-click}
           (d/div {:class ["flex-align-item-center" "gap-05em"]}
                  (d/a {:on-click on-click-fav}
                       ($ icon {:name "favorite" :class ["hoverable" (when fav? "fav")]}))
                  (d/b {:class "clamp-text"} name))
           (d/img {:src (str "/cover/" id ".jpeg")})
           (d/small "Released " (u/format-released released))
           (d/small "Tracks: " tracks)
           (d/small "Play time: " (u/format-playtime length))
           (d/small "Price: " (u/format-price price)))))


(defnc ArtistInfo [{:keys [artist]}]
  (let [favs    (use-resource ::favs "/api/account/like" {:parser set})
        set-fav (fn [id fav?]
                  (resource/mutate favs {:uri    "/api/account/like/:album-id"
                                         :params {:album-id id}
                                         :body   {:like fav?}}))]
    (<> (d/div (d/b (:artist/name artist)))
        (d/div (d/i (:artist/disambiguation artist)))
        (d/br)
        (d/div {:class ["grid-auto-cols-min-200px" "grid-auto-cols-max-05fr" "gap-2em-1em"]}
               (for [album (:artist/albums artist)
                     :let  [id (:album/id album)]]
                 ($ AlbumCard {:key     id
                               :album   album
                               :fav?    (contains? (:body favs) id)
                               :set-fav set-fav}))))))


(defnc ArtistPage [match]
  (let [artist-id (-> match :route :parameters :path :id)
        artist    (use-resource ::artist "/api/music/artist/:artist-id" {:params {:artist-id artist-id}})]
    (d/div
     (case (:status artist)
       :pending (d/div "Loading...")
       :error (d/div "Error!")
       (:ok :refreshing) ($ ArtistInfo {:artist (:body artist)})))))
