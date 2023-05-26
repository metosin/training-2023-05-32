(ns training.server.db.jdbc
  (:require [next.jdbc]))


(defn- connectable [ctx]
  (or (-> ctx ::tx) (-> ctx :system :ds)))


(defn execute!
  ([ctx sqlvec]
   (execute! ctx sqlvec nil))
  ([ctx sqlvec opts]
   (next.jdbc/execute! (connectable ctx) sqlvec opts)))


(defn execute-one!
  ([ctx sqlvec]
   (execute-one! ctx sqlvec nil))
  ([ctx sqlvec opts]
   (next.jdbc/execute-one! (connectable ctx) sqlvec opts)))


(defn with-tx-middleware [handler]
  (fn [req]
    (next.jdbc/with-transaction [tx (-> req :system :ds)]
      (-> (assoc req ::tx tx)
          (handler)))))