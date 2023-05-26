(ns training.server.api.session.handlers
  (:require [ring.util.http-response :as resp]
            [training.server.domain.account :as account]
            [training.server.api.session.core :as session]))


(defn check-session [req]
  (if-let [user-info (session/get-session req (session/get-session-key req))]
    (resp/ok {:user user-info})
    (resp/forbidden {:message "no session found"})))


(defn login [req]
  (let [login-form (-> req :parameters :body)]
    (if-let [account (account/find-account-by-username-password req (:username login-form) (:password login-form))]
      (let [new-session-key (session/make-session-key)
            user-info       (update-keys account name)]
        (session/set-session req new-session-key user-info)
        (-> (resp/ok {:user user-info})
            (session/set-session-cookie new-session-key)))
      (-> (resp/forbidden {:message "unknown user or wrong password"})
          (session/clear-session-cookie)))))


(defn logout-on-post [req]
  (session/remove-session req (session/get-session-key req))
  (-> (resp/ok {:message "logout"})
      (session/clear-session-cookie)))


(defn logout-on-get [req]
  (session/remove-session req (session/get-session-key req))
  (-> (resp/found "/")
      (session/clear-session-cookie)))
