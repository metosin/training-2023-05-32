(ns training.web.routing
  (:require [reitit.frontend :as rf]
            [reitit.frontend.easy :as rfe]
            [reitit.coercion.malli :as rcm]
            [training.web.state :as state]
            [training.web.view.front-page :as front]
            [training.web.view.about-page :as about]
            [training.web.view.artist-page :as artist]))


(defn use-route
  ([] (use-route nil))
  ([transformers] (state/use-app-state (concat [:route] transformers))))


(def routes
  [["/" {:name  :frontpage
         :view  front/FrontPage
         :class "nav-home"
         :nav   {:image "/image/epes-logo.png"
                 :index 0}}]
   ["/artist" {:name       :artists
               :view       artist/ArtistsPage
               :parameters {:query [:map [:search {:default ""} :string]]}
               :nav        {:label "Artists"
                            :index 1}}]
   ["/artist/:id" {:name       :artist
                   :view       artist/ArtistPage
                   :parameters {:path [:map [:id :string]]}}]
   ["/about" {:name :about
              :view about/AboutPage
              :nav  {:label "About"
                     :index 2}}]])


(def router (rf/router routes {:data {:coercion rcm/coercion}}))


(defn init! []
  (rfe/start!
   router
   (fn [match]
     (swap! state/app-state assoc :route match))
   {:use-fragment false}))
