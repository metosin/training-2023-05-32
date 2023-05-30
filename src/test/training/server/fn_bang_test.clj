(ns training.server.fn-bang-test
  (:require [clojure.test :refer [deftest testing is]]
            [matcher-combinators.test]
            [training.server.fn-bang :as fn-bang :refer [fn!]]))



;;
;; fn!
;;

(deftest fn!-tests
  (testing "fn! works as a function"
    (let [f (fn! [x y] (+ x y))]
      (is (= 3 (f 1 2)))))
  (testing "fn! is fn?"
    (let [f (fn! [x y] (+ x y))]
      (is (fn? f))))
  (testing "fn! is ifn?"
    (let [f (fn! [x y] (+ x y))]
      (is (ifn? f))))
  (testing "fn! can be used in closure"
    (let [f (let [a 1]
              (fn! [b] (+ a b)))]
      (is (= 3 (f 2)))))
  (testing "fn! can be used in closure 2"
    (let [f' (fn [a]
               (fn! [b] (+ a b)))
          f  (f' 1)]
      (is (= 3 (f 2)))))
  (testing "fn! functions equality"
    (testing "equal when args and body are equal"
      (let [[f1 f2] [(fn! [a b] (+ a b))
                     (fn! [a b] (+ a b))]]
        (is (= f1 f2))))
    (testing "not equal when args differ"
      (let [[f1 f2] [(fn! [a b] (+ a b))
                     (fn! [b a] (+ a b))]]
        (is (not (= f1 f2)))))
    (testing "not equal when body differs"
      (let [[f1 f2] [(fn! [a b] (+ a b))
                     (fn! [a b] (+ b a))]]
        (is (not (= f1 f2)))))
    (testing "equals when closures are equal"
      (let [[f1 f2] [(let [a 1]
                       (fn! [b] (+ a b)))
                     (let [a 1]
                       (fn! [b] (+ a b)))]]
        (is (= f1 f2))))
    (testing "not equal when closures differ"
      (let [[f1 f2] [(let [a 1]
                       (fn! [b] (+ a b)))
                     (let [a 2]
                       (fn! [b] (+ a b)))]]
        (is (not (= f1 f2))))))
  (testing "hash code"
    (testing "match with equal functions"
      (let [[f1 f2] [(fn! [a b] (+ a b))
                     (fn! [a b] (+ a b))]]
        (is (= (hash f1) (hash f2)))))
    (testing "now not match with unequal functions"
      (let [[f1 f2] [(fn! [a b] (+ a b))
                     (fn! [a b] (+ b a))]]
        (is (not (= (hash f1) (hash f2)))))))
  (testing "metadata has closure, params and body"
    (let [f (let [a 1]
              (fn! [b] (+ a b)))]
      ; Just test against :env, :params, and :body, others vary between clj and cljs
      (is (= {:env    {'a 1}
              :params ['b]
              :body   '((+ a b))}
             (select-keys (-> f (meta) ::fn-bang/fn!)
                          [:env :params :body]))))))


(defn correct-name-symbol? [expected-symbol]
  (fn [s]
    ; When running from kaocha, ns is always 'user. Don't know why.
    (or (= s (symbol "user" (name expected-symbol)))
        (= s (symbol "training.server.fn-bang-test" (name expected-symbol))))))


(deftest fn!-parsing-tests
  (testing "minimal fn!"
    (let [f (fn! [])]
      (is (match? {::fn-bang/fn! {:name   (correct-name-symbol? 'fn!)
                                  :env    {}
                                  :params []
                                  :body   nil}}
                  (meta f)))))
  (testing "fn! with body"
    (let [f (fn! [] (+ 1 2))]
      (is (match? {::fn-bang/fn! {:name   (correct-name-symbol? 'fn!)
                                  :env    {}
                                  :params []
                                  :body   '((+ 1 2))}}
                  (meta f)))))
  (testing "fn! with body and args"
    (let [f (fn! [a b] (+ a b))]
      (is (match? {::fn-bang/fn! {:name   (correct-name-symbol? 'fn!)
                                  :env    {}
                                  :params ['a 'b]
                                  :body   '((+ a b))}}
                  (meta f)))))
  (testing "fn! with body, args and metadata"
    (let [f (fn! {:doc "fancy"} [a b] (+ a b))]
      (is (match? {:doc         "fancy"
                   ::fn-bang/fn! {:name   (correct-name-symbol? 'fn!)
                                  :env    {}
                                  :params ['a 'b]
                                  :body   '((+ a b))}}
                  (meta f)))))
  (testing "fn! with name, body, args and metadata"
    (let [f (fn! foo {:doc "fancy"} [a b] (+ a b))]
      (is (match? {:doc         "fancy"
                   ::fn-bang/fn! {:name   (correct-name-symbol? 'foo)
                                  :env    {}
                                  :params ['a 'b]
                                  :body   '((+ a b))}}
                  (meta f)))))
  (testing "fn! with name body, args"
    (let [f (fn! foo [a b] (+ a b))]
      (is (match? {::fn-bang/fn! {:name   (correct-name-symbol? 'foo)
                                  :env    {}
                                  :params ['a 'b]
                                  :body   '((+ a b))}}
                  (meta f)))))
  (testing "fn! as closure"
    (let [f (let [a 1]
              (fn! [b] (+ a b)))]
      (is (match? {::fn-bang/fn! {:name   (correct-name-symbol? 'fn!)
                                  :env    {'a 1}
                                  :params ['b]
                                  :body   '((+ a b))}}
                  (meta f))))))
