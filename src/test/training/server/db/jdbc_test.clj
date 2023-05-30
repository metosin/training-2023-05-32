(ns training.server.db.jdbc-test
  (:require [clojure.test :as test :refer [deftest testing is]]
            [matcher-combinators.test]
            [next.jdbc]
            [training.server.db.jdbc :as jdbc]
            [training.server.db.tx :refer [with-tx-middleware]]
            [training.db-fixture :refer [*test-db-ds*]]))


(test/use-fixtures :once
  training.db-fixture/with-test-db)


(deftest execute-one!-test
  (let [ctx {:system {:ds *test-db-ds*}}]
    (testing "Executes SQL using DS"
      (is (match? {:now (partial instance? java.util.Date)}
                  (jdbc/execute-one! ctx ["select now() as now"]))))
    (testing "Executes SQL using TX if available"
      (let [handler (with-tx-middleware (fn [req] (jdbc/execute-one! req ["select now() as now"])))]
        (is (match? {:now (partial instance? java.util.Date)}
                    (handler ctx)))))))


(deftest execute!-test
  (let [ctx {:system {:ds *test-db-ds*}}]
    (testing "Executes SQL using DS"
      (is (match? [{:now (partial instance? java.util.Date)}]
                  (jdbc/execute! ctx ["select now() as now"]))))
    (testing "Executes SQL using TX if available"
      (let [handler (with-tx-middleware (fn [req] (jdbc/execute! req ["select now() as now"])))]
        (is (match? [{:now (partial instance? java.util.Date)}]
                    (handler ctx)))))))

