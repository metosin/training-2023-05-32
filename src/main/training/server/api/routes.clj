(ns training.server.api.routes
  (:require [ring.util.http-response :as resp]
            [training.server.db.middleware :as db]
            [training.server.api.session :as session]))


(def routes
  ["/api" {:middleware [db/tx-middleware]}
   ["/hello" {:get {:handler (fn [_req]
                               (resp/ok {:message "All systems go!!!"}))}}]
   ["/session" session/routes]])
