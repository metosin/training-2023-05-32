(ns training.server.db.hug
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [hugsql.core :as hugsql]
            [training.server.db.jdbc :as jdbc]))


(defonce ^:private domains (atom {}))


(defn register-domain [domain-key]
  {:pre [(keyword? domain-key)
         (nil? (namespace domain-key))]}
  (let [sql-file   (-> domain-key
                       (name)
                       (str/replace "." "/")
                       (str ".sql")
                       (io/resource)
                       (or (throw (ex-info "can't find SQL file for domain" {:domain-key domain-key}))))
        sqlvec-fns (-> (hugsql/map-of-sqlvec-fns sql-file {:fn-suffix nil})
                       ; Add domain-key as namespace to keys
                       (update-keys (comp (partial keyword (name domain-key)) name)))]
    ; Remove all existing functions for domain-key just to keep domains
    ; map clean of possible leftover entries:
    (swap! domains (fn [data]
                     (->> data
                          (remove (fn [[k]] (= (namespace k) (name domain-key))))
                          (into sqlvec-fns))))))


(defn- sqlvec [query params]
  (let [sqlvec-fn (-> (get-in @domains [query :fn])
                      (or (throw (ex-info (str "unknown HugSQL query: " (pr-str query))
                                          {:query query}))))]
    (sqlvec-fn params)))


(defn execute!
  ([ctx query] (execute! ctx query nil nil))
  ([ctx query params] (execute! ctx query params nil))
  ([ctx query params opts]
   (jdbc/execute! ctx (sqlvec query params) opts)))


(defn execute-one!
  ([ctx] (execute-one! ctx nil nil nil))
  ([ctx query] (execute-one! ctx query nil nil))
  ([ctx query params] (execute-one! ctx query params nil))
  ([ctx query params opts]
   (jdbc/execute-one! ctx (sqlvec query params) opts)))


(comment

  (let [ctx {:system {:ds (:ds user/system)}}]
    (execute-one! ctx
                  :training.server.domain.music/get-artist-by-id
                  {:artist-id "d87e52c5-bb8d-4da8-b941-9f4928627dc8"}))
  ;; =>  #:artist{:id             "d87e52c5-bb8d-4da8-b941-9f4928627dc8"
  ;;              :name           "ABBA"
  ;;              :disambiguation "Swedish pop group"}
  )