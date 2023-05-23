(ns training.server.db.honey
  (:require [next.jdbc :as jdbc]
            [honey.sql :as sql]))


(defn execute! [ctx query]
  (jdbc/execute! (-> ctx :system :ds) (sql/format query)))


(defn execute-one! [ctx query]
  (jdbc/execute-one! (-> ctx :system :ds) (sql/format query)))


(comment
  (sql/format {:select [:foo]
               :from   :bar
               :where  [[:starts_with :id "baba"]]}))