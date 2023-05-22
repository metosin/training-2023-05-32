(ns training.web.routing
  (:require [reitit.frontend :as rf]
            [reitit.frontend.easy :as rfe]
            [reitit.coercion.malli :as rcm]
            [training.web.state :as state]
            [training.web.routes :as routes]))


(defn use-route
  ([] (use-route nil))
  ([transformers] (state/use-app-state (concat [:route] transformers))))


(def router (rf/router routes/routes {:data {:coercion rcm/coercion}}))


(defn init! []
  (rfe/start!
   router
   (fn [match]
     (swap! state/app-state assoc :route match))
   {:use-fragment false}))
