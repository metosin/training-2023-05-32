(ns training.server.db.ds
  (:require [clojure.tools.logging :as log]
            [next.jdbc :as jdbc]
            [next.jdbc.connection]
            [training.server.db.json])
  (:import (com.zaxxer.hikari HikariDataSource)))


(defn make-datasource [db-spec]
  (let [ds (next.jdbc.connection/->pool HikariDataSource db-spec)]
    ; Ensure we have connectivity to DB
    (with-open [conn (jdbc/get-connection ds)]
      (log/debug "Testing DB connection...")
      (jdbc/execute-one! conn ["select now()"]))
    (log/info "DB pool ready")
    ds))


(defn close-datasource [ds]
  (when ds
    (-> (jdbc/get-datasource ds)
        (.close))
    (log/info "DB pool stopped")))


(comment

  (defn add-job
    ([ds command payload]
     (jdbc/execute! ds ["select graphile_worker.add_job(?, ?)" command payload]))
    ([ds command payload queue]
     (jdbc/execute! ds ["select graphile_worker.add_job(?, ?, ?)" command payload queue])))

  (jdbc/with-transaction [tx (-> @user/system :ds)]
    (add-job tx "pause" {:delay 2000} "foo")
    (add-job tx "echo" {:foo "bar 1"} "foo")
    (add-job tx "pause" {:delay 3000} "foo")
    (add-job tx "echo" {:foo "bar 2"} "foo")
    (add-job tx "pause" {:delay 3000} "foo")
    (add-job tx "echo" {:foo "bar 3"} "foo"))

  ;
  )