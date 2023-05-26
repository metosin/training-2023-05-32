(ns training.server.api.session.core
  (:require [ring.util.http-response :as resp]
            [training.server.redis.core :as redis]
            [training.server.domain.account :as account])
  (:import (java.time Duration)))


(def ^Duration session-expiration (Duration/ofHours 1))


;;
;; Cookie utils:
;;


(defn get-session-key [req]
  (-> req :cookies (get "session") :value))


(defn set-session-cookie [resp value]
  (assoc resp :cookies {"session" {:value     value
                                   :max-age   (.toSeconds session-expiration)
                                   :http-only true
                                   :same-site :strict}}))


(defn clear-session-cookie [resp]
  (assoc resp :cookies {"session" {:value     ""
                                   :http-only true
                                   :same-site :strict
                                   :expires   "Thu, 01 Jan 1970 00:00:00 GMT"}}))


;;
;; Session key utils:
;;


(def session-key-chars (->> (concat (range (int \a) (inc (int \z)))
                                    (range (int \A) (inc (int \Z)))
                                    (range (int \0) (inc (int \9))))
                            (map char)
                            (vec)))


(defn make-session-key []
  (apply str "session:" (repeatedly 24 (partial rand-nth session-key-chars))))


;;
;; Redis session storage:
;;


(defn get-session [req session-key]
  (when session-key
    (let [session-data (redis/hget req session-key)]
      ; Redis returns empty map when entry does not exist:
      (when (seq session-data)
        session-data))))


(defn set-session [req session-key session-data]
  (redis/hset req session-key session-data)
  (redis/expire req session-key session-expiration)
  req)


(defn remove-session [req session-key]
  (when session-key
    (redis/del req session-key))
  req)


;;
;; Handlers:
;;


(defn check-session [req]
  (if-let [user-info (get-session req (get-session-key req))]
    (resp/ok {:user user-info})
    (resp/forbidden {:message "no session found"})))


(defn login [req]
  (let [login-form (-> req :parameters :body)]
    (if-let [account (account/find-account-by-username-password req (:username login-form) (:password login-form))]
      (let [new-session-key (make-session-key)
            user-info       (update-keys account name)]
        (set-session req new-session-key user-info)
        (-> (resp/ok {:user user-info})
            (set-session-cookie new-session-key)))
      (-> (resp/forbidden {:message "unknown user or wrong password"})
          (clear-session-cookie)))))


(defn logout-on-post [req]
  (remove-session req (get-session-key req))
  (-> (resp/ok {:message "logout"})
      (clear-session-cookie)))


(defn logout-on-get [req]
  (remove-session req (get-session-key req))
  (-> (resp/found "/")
      (clear-session-cookie)))
