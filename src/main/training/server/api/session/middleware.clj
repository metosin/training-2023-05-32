(ns training.server.api.session.middleware
  (:require [clojure.tools.logging :as log]
            [ring.util.http-response :as resp]
            [training.server.domain.account :as account]
            [training.server.api.session.core :as core]))


(defn- cookie-session [req]
  (when-let [session-key (core/get-session-key req)]
    (core/get-session req session-key)))


(defn- apikey-session [req]
  (when-let [apikey (get-in req [:headers "x-apikey"])]
    (if-let [account (account/find-account-by-apikey req apikey)]
      (do (log/infof "apikey successful: %s => %s" apikey (:account/id account))
          (update-keys account name))
      (do (log/warnf "use of unknown apikey: apikey=%s" apikey)
          (resp/forbidden!)))))


(defn session-middleware [handler]
  (fn [req]
    (if-let [session (or (cookie-session req)
                         (apikey-session req))]
      (handler (assoc req :training.server.api.session/session session))
      (handler req))))


(defn require-session-middleware [handler]
  (fn [req]
    (when-not (contains? req :training.server.api.session/session)
      (log/warn "request without session rejected")
      (resp/forbidden! {:message "session required"}))
    (handler req)))


(defn require-role-middleware [handler required-role]
  (let [required-roles (if (keyword? required-role)
                         #{(name required-role)}
                         (apply set (map name required-role)))]
    (fn [req]
      (let [user-role (get-in req [:training.server.api.session/session "role"])]
        (when-not (required-roles user-role)
          (log/warnf "request without role rejected, required: %s user: %s"
                     (pr-str required-roles)
                     (pr-str user-role))
          (resp/forbidden! {:message "required role missing"})))
      (handler req))))

