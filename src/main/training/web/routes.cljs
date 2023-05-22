(ns training.web.routes
  (:require [training.web.view.front-page  :refer [FrontPage]]
            [training.web.view.about-page :refer [AboutPage]]
            [training.web.view.item-page :refer [ItemPage]]
            [training.web.view.artist-page :as artist]))


(def routes
  [["/" {:name ::frontpage 
         :view FrontPage
         :nav [0 "Home"]}] 
   ["/artist" {:name ::artists
               :view artist/ArtistsPage 
               :nav [1 "Artists"]}]
   ["/artist/:id" {:name ::artist 
                   :view artist/ArtistPage}]
   ["/about" {:name ::about 
              :view AboutPage
              :nav [2 "About"]}]
   ["/item/:id" {:name ::item 
                 :view ItemPage 
                 :parameters {:path {:id :int}}}]])
