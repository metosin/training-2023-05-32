(ns training.fx-test-utils
  (:require [clojure.test :as test :refer [is]]
            [matcher-combinators.test]
            [training.server.fn-bang :as fn-bang]))


; For some reason macros are evaluated from 'user ns when test are
; run by kaocha. Allow expected fx function namespace to be 'user.

(defn allow-user-ns [expected]
  (if (contains? expected :name)
    (update expected :name (fn [expected-symbol]
                             (fn [actual-symbol]
                               (or (= expected-symbol actual-symbol)
                                   (and (= (namespace actual-symbol) "user")
                                        (= (name actual-symbol) (name expected-symbol)))))))
    expected))


(defn fx? [expected]
  (fn [fx]
    (let [fx-meta  (-> (meta fx) ::fn-bang/fn!)
          expected (allow-user-ns expected)]
      (is (match? expected fx-meta)))))


