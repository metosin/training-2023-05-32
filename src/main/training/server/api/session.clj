(ns training.server.api.session
  (:require [ring.util.http-response :as resp]))


(defn set-session-cookie [resp value]
  (assoc resp :cookies {"session" {:value     value
                                   :http-only true
                                   :same-site :strict}}))


(defn clear-session-cookie [resp]
  (assoc resp :cookies {"session" {:value     ""
                                   :http-only true
                                   :same-site :strict
                                   :expires   "Thu, 01 Jan 1970 00:00:00 GMT"}}))



(def routes
  [""
   ["" {:post {:handler (fn [req]
                          (if-let [user (-> req :cookies (get "session") :value)]
                            (resp/ok {:user user})
                            (resp/forbidden {:message "no session found"})))}}]
   ["/login" {:post {:parameters {:body [:map
                                         [:user :string]
                                         [:password :string]]}
                     :handler    (fn [req]
                                   (let [user (-> req :parameters :body :user)]
                                     (if (not= user "haxor")
                                       (-> (resp/ok {:user user})
                                           (set-session-cookie user))
                                       (-> (resp/forbidden {:message "unknown user or wrong password"})
                                           (clear-session-cookie)))))}}]
   ["/logout" {:post {:handler (fn [_req]
                                 (-> (resp/ok {:message "logout"})
                                     (clear-session-cookie)))}
               :get  {:handler (fn [_req]
                                 (-> (resp/found "/")
                                     (clear-session-cookie)))}}]])
