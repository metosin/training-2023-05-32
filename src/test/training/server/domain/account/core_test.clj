(ns training.server.domain.account.core-test
  (:require [clojure.test :as test :refer [deftest testing is]]
            [matcher-combinators.test]
            [next.jdbc :as jdbc]
            [training.server.domain.account.core :as account.core]
            [training.db-fixture :refer [*test-db-ds*]]))


(test/use-fixtures :once
  training.db-fixture/with-test-db
  training.db-fixture/with-test-fixture)


(deftest get-account-by-id-test

  (testing "returns nil when account is not found"
    (let [ctx {:system {:ds *test-db-ds*}}]
      (is (nil? (account.core/get-account-by-id ctx "foo")))))

  (testing "returns account details when account is found"
    (let [ctx        {:system {:ds *test-db-ds*}}
          tina       (jdbc/execute-one! *test-db-ds* ["select account.id from epes.account where account.username = ?" "tina"])
          account-id (:account/id tina)
          account    (account.core/get-account-by-id ctx account-id)]
      (is (match? {:account/id       string?
                   :account/fullname "Tina Turner"
                   :account/username "tina"
                   :account/role     "admin"}
                  account))
      (is (not (contains? account :account/password))))))


(deftest find-account-by-username-password-test

  (testing "returns nil when account is not found"
    (let [ctx {:system {:ds *test-db-ds*}}]
      (is (nil? (account.core/find-account-by-username-password ctx "foo" "foo")))
      (is (nil? (account.core/find-account-by-username-password ctx "tina" "foo")))))

  (testing "returns account details when account is found"
    (let [ctx     {:system {:ds *test-db-ds*}}
          account (account.core/find-account-by-username-password ctx "tina" "tina")]
      (is (match? {:account/id       string?
                   :account/fullname "Tina Turner"
                   :account/username "tina"
                   :account/role     "admin"}
                  account))
      (is (not (contains? account :account/password))))))
