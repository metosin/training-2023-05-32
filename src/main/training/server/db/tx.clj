(ns training.server.db.tx
  (:require [next.jdbc]))


(defn with-tx-middleware [handler]
  (fn [req]
    (next.jdbc/with-transaction [tx (-> req :system :ds)]
      (-> (assoc req ::tx tx)
          (handler)))))
