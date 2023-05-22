(ns training.server.db.honey
  (:require [next.jdbc :as jdbc]
            [honey.sql :as sql]))


(defn execute! [{tx :tx} query]
  (jdbc/execute! tx (sql/format query)))


(defn execute-one! [{tx :tx} query]
  (jdbc/execute-one! tx (sql/format query)))


(comment
  (sql/format {:select [:foo]
               :from   :bar
               :where  [[:starts_with :id "baba"]]}))