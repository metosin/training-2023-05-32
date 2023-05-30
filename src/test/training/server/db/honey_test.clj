(ns training.server.db.honey-test
  (:require [clojure.test :as test :refer [deftest is]]
            [matcher-combinators.test]
            [next.jdbc]
            [training.server.db.honey :as honey]
            [training.db-fixture :refer [*test-db-ds*]]))


(test/use-fixtures :once
  training.db-fixture/with-test-db)


(deftest execute-one!-test
  (let [ctx {:system {:ds *test-db-ds*}}]
    (is (match? {:ts (partial instance? java.util.Date)}
                (honey/execute-one! ctx {:select [[[:now] :ts]]})))))


(deftest execute!-test
  (let [ctx {:system {:ds *test-db-ds*}}]
    (is (match? [{:ts (partial instance? java.util.Date)}]
                (honey/execute! ctx {:select [[[:now] :ts]]})))))

