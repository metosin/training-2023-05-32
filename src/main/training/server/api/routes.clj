(ns training.server.api.routes
  (:require [ring.util.http-response :as resp]
            [training.server.api.session :as session]
            [training.server.api.music :as music]))


(def routes
  ["/api"
   ["/hello" {:get {:handler (fn [_req]
                               (resp/ok {:message "All systems go!!!"}))}}]
   ["/session" session/routes]
   ["/music" music/routes]])
