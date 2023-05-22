(ns training.server.db.middleware
  (:require [next.jdbc :as jdbc]))


(defn tx-middleware [handler]
  (fn [req]
    (jdbc/with-transaction [tx (-> req :system :ds)]
      (handler (assoc req :tx tx)))))
