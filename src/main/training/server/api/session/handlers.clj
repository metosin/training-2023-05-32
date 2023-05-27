(ns training.server.api.session.handlers
  (:require [ring.util.http-response :as resp]
            [training.server.domain.account.core :as account]
            [training.server.api.session :as session]
            [training.server.api.session.core :as sc]))


(defn check-session [req]
  (if-let [user-info (::session/session req)]
    (resp/ok user-info)
    (resp/forbidden {:message "no session found"})))


(defn login [req]
  (let [login-form (-> req :parameters :body)]
    (if-let [account (account/find-account-by-username-password req (:username login-form) (:password login-form))]
      (let [new-session-key (sc/make-session-key)
            user-info       (update-keys account name)]
        (sc/set-session req new-session-key user-info)
        (-> (resp/ok user-info)
            (sc/set-session-cookie new-session-key)))
      (-> (resp/forbidden {:message "unknown user or wrong password"})
          (sc/clear-session-cookie)))))


(defn logout-on-post [req]
  (sc/remove-session req (sc/get-cookie-session-key req))
  (-> (resp/ok {:message "logout"})
      (sc/clear-session-cookie)))


(defn logout-on-get [req]
  (sc/remove-session req (sc/get-cookie-session-key req))
  (-> (resp/found "/")
      (sc/clear-session-cookie)))
