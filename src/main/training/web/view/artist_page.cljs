(ns training.web.view.artist-page
  (:require [helix.core :as hx :refer [$ defnc <>]]
            [helix.dom :as d]
            [reitit.frontend.easy :as rfe]
            [training.web.util :refer [icon]]
            [training.web.use-debounce :refer [use-debounce]]
            [training.web.use-resource :as resource :refer [use-resource]]))


(defnc ArtistsPage [match]
  (let [search                (-> match :route :parameters :query :search)
        [search' set-search'] (use-debounce search)
        artist                (use-resource ::artists "/api/music/artist" {:query {:name search'}})]
    (d/div
     (d/input {:type        "text"
               :value       search
               :placeholder "Artist name"
               :auto-focus  true
               :on-change   (fn [^js e]
                              (let [search (-> e .-target .-value)]
                                (rfe/replace-state :artists nil {:search search})
                                (set-search' search)))})
     (case (:status artist)
       :pending (d/div "Loading...")
       :error (d/div "Error!")
       (:ok :refreshing) (d/table
                          {:role  "grid"}
                          (d/thead
                           (d/tr
                            (d/th "Artist")
                            (d/th "Info")
                            (d/th "Albums")))
                          (d/tbody
                           (for [artist (:body artist)
                                 :let [{:artist/keys [id name disambiguation]} artist]]
                             (d/tr {:key      id
                                    :class    "cursor-pointer"
                                    :on-click (fn [_] (rfe/push-state :artist {:id id}))}
                                   (d/td name)
                                   (d/td disambiguation)
                                   (d/td (:albums artist))))))))))


(defn format-released [^js released]
  (let [y (+ 1900 (.getYear released))
        m (+ 1 (.getMonth released))
        d (.getDate released)]
    (str (when (< d 10) "0") d "."
         (when (< m 10) "0") m "."
         y)))


(defn format-playtime [length]
  (let [hours   (int (/ length 1000.0 60.0 60.0))
        length  (- length (* hours 1000.0 60.0 60.0))
        minutes (int (/ length 1000.0 60.0))
        length  (- length (* minutes 1000.0 60.0))
        seconds (int (/ length 1000.0))]
    (str hours "h "
         (when (< minutes 10) "0")
         minutes "min "
         (when (< seconds 10) "0")
         seconds "sec")))


(defn format-price [price]
  (str (-> price (/ 100.0) (.toFixed 2)) "€"))


(defnc ArtistPage [match]
  (let [artist-id (-> match :route :parameters :path :id)
        artist    (use-resource ::artist "/api/music/artist/:artist-id" {:params {:artist-id artist-id}})
        favs      (use-resource ::favs "/api/account/self/like" {:parser set})]
    (d/div
     {:class "artist"}
     (case (:status artist)
       :pending (d/div "Loading...")
       :error (d/div "Error!")
       (:ok :refreshing)
       (let [{:artist/keys [name disambiguation]} (:body artist)]
         (<> (d/div (d/b name))
             (d/div (d/i disambiguation))
             (d/div
              {:class ["grid-auto-cols" "gird-auto-cols-200" "gap-10"]}
              (for [album (:artist/albums (:body artist))
                    :let [{:album/keys [id name released]} album
                          {:keys [tracks length price]} album
                          fav? (contains? (:body favs) id)]]
                (d/div {:class    ["cursor-pointer" "flex-direction-column" "flex-align-items-stretch"]
                        :key      id
                        :on-click (fn [e]
                                    (.preventDefault e)
                                    (println "ALBUM: click:" e)
                                    ;; TODO: Navigate to album
                                    #_(rfe/push-state :artist {:id id}))}
                       (d/div {:class ["flex-align-item-center" "gap-05"]}
                              (d/b {:class "clamp-text"} name)
                              (d/a {:on-click (fn [e]
                                                (.preventDefault e)
                                                (.stopPropagation e)
                                                (resource/mutate favs {:uri    "/api/account/self/like/:album-id"
                                                                       :params {:album-id id}
                                                                       :body   {:like (not fav?)}}))}
                                   ($ icon {:name  "favorite"
                                            :class ["hoverable" (when fav? "fav")]})))
                       (d/img {:src   (str "/cover/" id ".jpeg")
                               :class "artist-albums-art"})
                       (d/div {:class "small"} "Released " (format-released released))
                       (d/div {:class "small"} "Tracks: " tracks)
                       (d/div {:class "small"} "Play time: " (format-playtime length))
                       (d/div {:class "small"} "Price: " (format-price price)))))))))))
