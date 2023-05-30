(ns training.server.domain.music-test
  (:require [clojure.test :as test :refer [deftest is]]
            [matcher-combinators.test]
            [training.server.domain.music :as music]
            [training.db-fixture :refer [*test-db-ds*]]))


(test/use-fixtures :once
  training.db-fixture/with-test-db
  training.db-fixture/with-test-fixture)


(deftest get-artists-by-name-test
  (let [req {:system     {:ds *test-db-ds*}
             :parameters {:query {:name  "ramm"
                                  :limit 10}}}]
    (is (match? {:status  200
                 :headers {"etag" #"\d+"}
                 :body    [{:artist/name "Rammstein"}]}
                (music/get-artists-by-name req)))))


(deftest get-albums-by-name-test
  (let [req {:system     {:ds *test-db-ds*}
             :parameters {:query {:name  "herzele"
                                  :limit 10}}}]
    (is (match? {:status  200
                 :headers {"etag" #"\d+"}
                 :body    [{:album/name "Herzeleid"}]}
                (music/get-albums-by-name req)))))