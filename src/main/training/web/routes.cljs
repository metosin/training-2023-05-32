(ns ^:dev/always training.web.routes
  (:require [training.web.view.front-page :as front]
            [training.web.view.about-page :as about]
            [training.web.view.artist-page :as artist]))


(def routes
  [["/" {:name :frontpage
         :view front/FrontPage
         :nav  [0 "Home"]}]
   ["/artist" {:name       :artists
               :view       artist/ArtistsPage
               :parameters {:query [:map [:search {:default ""} :string]]}
               :nav        [1 "Artists"]}]
   ["/artist/:id" {:name       :artist
                   :view       artist/ArtistPage
                   :parameters {:path [:map [:id :string]]}}]
   ["/about" {:name :about
              :view about/AboutPage
              :nav  [2 "About"]}]])
