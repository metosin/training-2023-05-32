(ns training.server.db.hug
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [hugsql.core :as hugsql]
            [training.server.db.jdbc :as jdbc]
            [training.server.fn-bang :refer [fn!]]))


(defonce ^:private domains (atom {}))


(defn register-domain [domain-key]
  {:pre [(keyword? domain-key)
         (nil? (namespace domain-key))]}
  (let [sql-file   (-> domain-key
                       (name)
                       (str/replace #"[.-]" {"." "/"
                                             "-" "_"})
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


(defn execute! [ctx query params]
  (jdbc/execute! ctx (sqlvec query params)))


(defn execute-one! [ctx query params]
  (jdbc/execute-one! ctx (sqlvec query params)))


;;
;; Fx:
;;


(defn execute-fx! [query params]
  (fn! execute-hugsql [ctx] (jdbc/execute! ctx (sqlvec query params))))


(defn execute-one-fx! [query params]
  (fn! execute-hugsql-one [ctx] (jdbc/execute-one! ctx (sqlvec query params))))
