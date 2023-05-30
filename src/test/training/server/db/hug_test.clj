(ns training.server.db.hug-test
  (:require [clojure.test :as test :refer [deftest testing is]]
            [matcher-combinators.test]
            [next.jdbc]
            [training.server.db.hug :as hugsql]
            [training.db-fixture :refer [*test-db-ds*]]))


(hugsql/register-domain :training.server.db.hug-test)


(test/use-fixtures :once
  training.db-fixture/with-test-db)


(deftest execute-one!-test
  (let [ctx {:system {:ds *test-db-ds*}}]
    (is (match? {:now (partial instance? java.util.Date)
                 :bar "Foo"}
                (hugsql/execute-one! ctx ::get-now {:foo "Foo"})))))


(deftest execute!-test
  (let [ctx {:system {:ds *test-db-ds*}}]
    (is (match? [{:now (partial instance? java.util.Date)
                  :bar "Foo"}]
                (hugsql/execute! ctx ::get-now {:foo "Foo"})))))

