(ns training.server.db.honey
  (:require [training.server.db.jdbc :as jdbc]
            [honey.sql :as sql]))


(defn execute!
  ([ctx query] (execute! ctx query nil))
  ([ctx query opts]
   (jdbc/execute! ctx (sql/format query) opts)))


(defn execute-one!
  ([ctx query] (execute-one! ctx query nil))
  ([ctx query opts]
   (jdbc/execute-one! ctx (sql/format query) opts)))
