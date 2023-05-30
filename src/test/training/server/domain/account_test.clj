(ns training.server.domain.account-test
  (:require [clojure.test :as test :refer [deftest testing is]]
            [matcher-combinators.test]
            [next.jdbc :as jdbc]
            [training.server.api.session :as session]
            [training.server.domain.account :as account]
            [training.db-fixture :refer [*test-db-ds*]]
            [training.server.fx :as fx]
            [training.fx-test-utils :refer [fx?]]
            [training.server.http.cache :as cache]))


(test/use-fixtures :once
  training.db-fixture/with-test-db
  training.db-fixture/with-test-fixture)


(deftest get-fav-by-account-id-test
  (let [james     (jdbc/execute-one! *test-db-ds* ["select account.id from epes.account where account.username = ?" "james"])
        james-id  (:account/id james)
        target-id "123"]

    (testing "Initially James has no likes"
      (let [req  {:system          {:ds *test-db-ds*}
                  ::session/session {"id" james-id}}
            resp (account/get-fav-by-account-id req)
            etag (get-in resp [:headers "etag"])]
        (is (match? {:status  200
                     :headers {"etag" string?}
                     :body    empty?}
                    resp))
        (testing "Making same request with correct etag returns 304 (Not modified)"
          (is (match? {:status 304}
                      (-> (assoc-in req [:headers cache/value-if-none-match] etag)
                          (account/get-fav-by-account-id)))))))

    (testing "James adds like"
      (let [req {:system          {:ds *test-db-ds*}
                 :parameters      {:path {:target-id target-id}
                                   :body {:like true}}
                 ::session/session {"id" james-id}}]
        (is (match? {:status 200}
                    (account/update-like req)))))

    (testing "James has one like"
      (let [req  {:system          {:ds *test-db-ds*}
                  :parameters      {:path {:account-id james-id}}
                  ::session/session {"id" james-id}}
            resp (account/get-fav-by-account-id req)]
        (is (match? {:status  200
                     :headers {"etag" #"\d{2,}"}
                     :body    [target-id]}
                    resp))))

    (testing "James removes like"
      (let [req {:system          {:ds *test-db-ds*}
                 :parameters      {:path {:account-id james-id
                                          :target-id  target-id}
                                   :body {:like false}}
                 ::session/session {"id" james-id}}]
        (is (match? {:status 200} (account/update-like req)))))

    (testing "James has no likes"
      (let [req  {:system          {:ds *test-db-ds*}
                  :parameters      {:path {:account-id james-id}}
                  ::session/session {"id" james-id}}
            resp (account/get-fav-by-account-id req)]
        (is (match? {:status 200
                     :body   empty?}
                    resp))))))


;;
;; Fx:
;;


#_(deftest get-fav-by-account-id-fx-test
    (let [req {::session/session {"id" "123"}}]
      (is (match? {:status 200
                   :body   (fx? {:name 'training.server.db.hug/execute-hugsql
                                 :env  {'query  ::account/get-fav-by-account-id
                                        'params {:account-id "123"}}})
                   ::fx/fx  {:post [(partial = cache/handle-etag)]}}
                  (account/get-fav-by-account-id-fx req)))))


#_(deftest update-like-fx-test
    (let [account-id      'test-account-id
          target-id       'test-target-id
          req             {::session/session {"id" account-id}
                           :parameters      {:path {:target-id target-id}}}
          expected-params {:account-id account-id
                           :target-id  target-id}]

      (testing "Adding like"
        (let [req            (assoc-in req [:parameters :body] {:like true})
              expected-query ::account/add-fav]
          (is (match? {:status 200
                       :body   {}
                       ::fx/fx  {:fx [(fx? {:name 'training.server.db.hug/execute-hugsql-one
                                            :env  {'query  expected-query
                                                   'params expected-params}})]}}
                      (account/update-like-fx req)))))

      (testing "Removing like"
        (let [req            (assoc-in req [:parameters :body] {:like false})
              expected-query ::account/remove-fav]
          (is (match? {:status 200
                       :body   {}
                       ::fx/fx  {:fx [(fx? {:name 'training.server.db.hug/execute-hugsql-one
                                            :env  {'query  expected-query
                                                   'params expected-params}})]}}
                      (account/update-like-fx req)))))))
