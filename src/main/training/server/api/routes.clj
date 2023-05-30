(ns training.server.api.routes
  (:require [ring.util.http-response :as resp]
            [training.server.api.session.routes :as session]
            [training.server.domain.music :as music]
            [training.server.domain.account :as account]))


(def routes
  ["/api"
   ["/ping" {:get {:handler (constantly (resp/ok {:message "All systems go!!!"}))}}]
   session/routes
   music/routes
   account/routes])
