(ns training.server.redis.pool
  (:require [training.server.redis.core :as redis])
  (:import (redis.clients.jedis Jedis JedisPool)))


(set! *warn-on-reflection* true)


(defn make-pool ^JedisPool [{:keys [host port]}]
  (JedisPool. ^String host (int port)))


(defn close-pool [^JedisPool pool]
  (when pool
    (.close pool)))


(defn get-client ^Jedis [^JedisPool pool]
  (.getResource pool))


(defn redis-middleware [handler]
  (fn [req]
    (with-open [client (get-client (-> req :system :redis))]
      (-> (assoc req ::redis/client client)
          (handler)))))
