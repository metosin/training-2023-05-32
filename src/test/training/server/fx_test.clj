(ns training.server.fx-test
  (:require [clojure.test :refer [deftest testing is]]
            [matcher-combinators.test]
            [ring.util.http-response :as resp]
            [training.server.fn-bang :as fn-bang :refer [defn! fn!]]
            [training.server.fx :as fx]))


(defn! x-must-be-pos [{x :x}]
  (pos? x))


(deftest assert-test
  (testing "Can add assetion"
    (is (match? {::fx/fx {:assert (partial = #{x-must-be-pos})}}
                (fx/assert {} x-must-be-pos))))
  (testing "Adding same assertion multiple times is nop"
    (is (match? {::fx/fx {:assert (partial = #{x-must-be-pos})}}
                (-> {}
                    (fx/assert x-must-be-pos)
                    (fx/assert x-must-be-pos)
                    (fx/assert x-must-be-pos))))))


(defn! y-must-be-gt-x
  {::fx/on-reject (resp/bad-request {:message "y must be gt x"})}
  [{:keys [x y]}]
  (> y x))


(def execute-asserts! #'fx/execute-asserts!)


(deftest execute-asserts!-test
  (testing "Asserts should pass"
    (let [ctx (-> {:x 1
                   :y 2}
                  (fx/assert x-must-be-pos)
                  (fx/assert y-must-be-gt-x))]
      (is (match? {:x 1
                   :y 2}
                  (execute-asserts! ctx)))))
  (testing "Assert on x should reject"
    (let [ctx (-> {:x 0
                   :y 2}
                  (fx/assert x-must-be-pos)
                  (fx/assert y-must-be-gt-x))]
      (is (thrown-match? clojure.lang.ExceptionInfo
                         {:type     ::resp/response
                          :response {:status 403}}
                         (execute-asserts! ctx)))))
  (testing "Assert on y should reject"
    (let [ctx (-> {:x 1
                   :y 0}
                  (fx/assert x-must-be-pos)
                  (fx/assert y-must-be-gt-x))]
      (is (thrown-match? clojure.lang.ExceptionInfo
                         {:type     ::resp/response
                          :response {:status 400
                                     :body   {:message "y must be gt x"}}}
                         (execute-asserts! ctx))))))


(def execute-fxs! #'fx/execute-fxs!)


(defn! inc-x [ctx]
  (update ctx :x inc))


(deftest execute-fx!-test
  (testing "fx is executes"
    (let [ctx (-> {:x 1}
                  (fx/fx inc-x))]
      (is (match? {:x 2}
                  (execute-fxs! ctx :fx)))))
  (testing "same fx can be added multiple times"
    (let [ctx (-> {:x 1}
                  (fx/fx inc-x)
                  (fx/fx inc-x)
                  (fx/fx inc-x))]
      (is (match? {:x 4}
                  (execute-fxs! ctx :fx))))))



(def execute-fn!-body #'fx/execute-fn!-body)


(deftest execute-fn!-test
  (testing "nil ctx is ol"
    (is (nil? (execute-fn!-body nil))))
  (testing "nil body is ok"
    (let [ctx {:x    1
               :y    2
               :body nil}]
      (is (match? {:x    1
                   :y    2
                   :body nil}
                  (execute-fn!-body ctx)))))
  (testing "non fn! body is not changed"
    (let [ctx {:x    1
               :y    2
               :body "foo"}]
      (is (match? {:x    1
                   :y    2
                   :body "foo"}
                  (execute-fn!-body ctx)))))
  (testing "fn! body is executes"
    (let [ctx {:x    1
               :y    2
               :body (fn! [{:keys [x y]}] (+ x y))}]
      (is (match? {:x    1
                   :y    2
                   :body 3}
                  (execute-fn!-body ctx))))))
