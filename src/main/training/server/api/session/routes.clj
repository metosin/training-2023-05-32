(ns training.server.api.session.routes
  (:require [training.server.api.session.handlers :as session]))


(def routes
  [""
   ["" {:post {:handler session/check-session}}]
   ["/login" {:post {:parameters {:body [:map
                                         [:username :string]
                                         [:password :string]]}
                     :handler    session/login}}]
   ["/logout" {:post {:handler session/logout-on-post}
               :get  {:handler session/logout-on-get}}]])
