(ns training.web.view.artist-page
  (:require [helix.core :as hx :refer [defnc <>]]
            [helix.dom :as d]
            [reitit.frontend.easy :as rfe]
            [training.web.use-resource :refer [use-resource]]))


(defnc ArtistsPage [match] 
  (let [search   (-> match :route :parameters :query :search) 
        resource (use-resource ::artists "/api/music/artist" {:query {:name search}})]
    (d/div
     (d/input {:type      "text"
               :value     search
               :on-change (fn [^js e]
                            (let [search (-> e .-target .-value)]
                              (rfe/replace-state :artists nil {:search search})))})
     (case (:status resource)
       :pending (d/div "Loading...")
       :error (d/div "Error!")
       (:ok :refreshing) (d/table
                          {:role  "grid"
                           :class "artists-table"}
                          (d/thead
                           (d/tr
                            (d/th "Artist")
                            (d/th "Into")
                            (d/th "Albums")))
                          (d/tbody
                           (for [{:artist/keys [id name disambiguation] :as artist} (:body resource)]
                             (d/tr
                              {:key      id
                               :on-click (fn [_] (rfe/push-state :artist {:id id}))}
                              (d/td name)
                              (d/td disambiguation)
                              (d/td (:albums artist))))))))))


(defnc ArtistPage [match]
  (let [artist-id (-> match :route :parameters :path :id)
        resource (use-resource ::artist "/api/music/artist/:artist-id" {:params {:artist-id artist-id}})]
    (d/div
     {:class "artist"}
     (case (:status resource)
       :pending (d/div "Loading...")
       :error (d/div "Error!")
       (:ok :refreshing)
       (let [{:artist/keys [name disambiguation]} (:body resource)]
         (<> (d/div (d/b name))
             (d/div (d/i disambiguation))
             (d/div 
              {:class "artist-albums"} 
              (for [{:album/keys [id name ^js released] :as album} (:artist/albums (:body resource))] 
                (d/div {:class    "artist-album"
                        :key      id
                        :on-click (fn [_] (rfe/push-state :artist {:id id}))}
                       (d/img {:src   (str "https://coverartarchive.org/release-group/" id "/front")
                               :class "artist-albums-art"})
                       (d/div
                        (d/div (d/b name))
                        (d/div {:class "small"}
                               "Released "
                               (let [y (+ 1900 (.getYear released))
                                     m (+ 1 (.getMonth released))
                                     d (.getDate released)]
                                 (str (when (< d 10) "0") d "."
                                      (when (< m 10) "0") m "."
                                      y)))
                        (d/div {:class "small"} "Tracks: " (:tracks album))
                        (d/div {:class "small"} "Play time: "
                               (let [length  (:length album)
                                     hours   (int (/ length 1000.0 60.0 60.0))
                                     length  (- length (* hours 1000.0 60.0 60.0))
                                     minutes (int (/ length 1000.0 60.0))
                                     length  (- length (* minutes 1000.0 60.0))
                                     seconds (int (/ length 1000.0))]
                                 (str hours "h " 
                                      (when (< minutes 10) "0")
                                      minutes "min " 
                                      (when (< seconds 10) "0")
                                      seconds "sec")))))))))))))
