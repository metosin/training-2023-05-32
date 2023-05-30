(ns training.server.db.honey
  (:require [training.server.db.jdbc :as jdbc]
            [honey.sql :as sql]
            [training.server.fn-bang :refer [fn!]]))


(defn execute! [ctx query]
  (jdbc/execute! ctx (sql/format query)))


(defn execute-one! [ctx query]
  (jdbc/execute-one! ctx (sql/format query)))


;;
;; Fx:
;;


(defn execute-fx! [query]
  (fn! [ctx] (jdbc/execute! ctx (sql/format query))))


(defn execute-one-fx! [query]
  (fn! [ctx] (jdbc/execute-one! ctx (sql/format query))))
