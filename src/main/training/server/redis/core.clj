(ns training.server.redis.core
  (:refer-clojure :exclude [get set dec inc])
  (:require [clojure.core :as c])
  (:import (redis.clients.jedis Jedis)))


(set! *warn-on-reflection* true)


(defn- millis ^long [v]
  (if (instance? java.time.Duration v)
    (.toMillis ^java.time.Duration v)
    (long v)))


;;
;; API:
;;


(defn exists [{::keys [client]} k]
  (.exists ^Jedis client ^String k))


(defn set
  ([{::keys [client]} k v]
   (.set ^Jedis client ^String k ^String v))
  ([{::keys [client]} k v expires]
   (.psetex ^Jedis client ^String k (millis expires) ^String v)))


(defn get [{::keys [client]} k]
  (.get ^Jedis client ^String k))


(defn del [{::keys [client]} k]
  (.del ^Jedis client ^String k))


(defn expire [{::keys [client]} k expire]
  (.pexpire ^Jedis client ^String k (millis expire)))


(defn get-set [{::keys [client]} k v]
  (.getSet ^Jedis client ^String k ^String v))


(defn hset
  ([{::keys [client]} k m]
   (.hset ^Jedis client ^String k ^java.util.Map m))
  ([{::keys [client]} k f v]
   (.hset ^Jedis client ^String k ^String f ^String v)))


(defn hget
  ([{::keys [client]} k]
   (into {} (.hgetAll ^Jedis client ^String k)))
  ([{::keys [client]} k a]
   (.hget ^Jedis client ^String k ^String a)))


(defn hinc
  ([{::keys [client]} k f]
   (.hincrBy ^Jedis client ^String k ^String f 1))
  ([{::keys [client]} k f v]
   (.hincrBy ^Jedis client ^String k ^String f (long v))))


(defn inc
  ([{::keys [client]} k]
   (.incr ^Jedis client ^String k))
  ([{::keys [client]} k v]
   (.incrBy ^Jedis client ^String k (long v))))


(defn dec
  ([{::keys [client]} k]
   (.decr ^Jedis client ^String k))
  ([{::keys [client]} k v]
   (.decrBy ^Jedis client ^String k (long v))))



(comment

  (require '[training.server.redis.pool :as client])
  (with-open [pool   (client/make-pool {:host "localhost"
                                        :port 6379})
              client (client/get-client pool)]
    (get client "foozaa")))

;; public Jedis getResource()
;; Overrides:
;; getResource in class Pool<Jedis>
;; returnResource
;; public void returnResource(Jedis resource)

