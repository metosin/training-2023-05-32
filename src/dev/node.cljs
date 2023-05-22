(ns node
  (:require [training.web.state :as state]))


(defn hello []
  "hi, node")


(comment
  (require '[training.web.http :as http])
  (require '[training.web.state :as state])

  @state/app-state
  (swap! state/app-state assoc :session nil)
  (-> (http/GET "/api/hello")
      (.then (fn [resp]
               (println (pr-str resp))))))
