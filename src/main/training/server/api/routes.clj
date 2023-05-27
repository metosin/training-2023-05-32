(ns training.server.api.routes
  (:require [ring.util.http-response :as resp]
            [training.server.api.session.routes :as session]
            [training.server.domain.music :as music]
            [training.server.domain.account :as account]))


(def routes
  ["/api"
   ["/hello" {:get {:handler (fn [_req]
                               (resp/ok {:message "All systems go!!!"}))}}]
   ["/session" session/routes]
   ["/music" music/routes]
   ["/account" account/routes]])
