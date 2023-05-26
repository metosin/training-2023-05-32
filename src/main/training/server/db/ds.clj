(ns training.server.db.ds
  (:require [clojure.tools.logging :as log]
            [next.jdbc :as jdbc]
            [next.jdbc.connection]
            [training.server.db.json])
  (:import (com.zaxxer.hikari HikariDataSource)))


(defn make-datasource [db-spec]
  (log/info "DB - Creating connection pool")
  (let [ds (next.jdbc.connection/->pool HikariDataSource db-spec)]
    (with-open [conn (jdbc/get-connection ds)]
      (jdbc/execute-one! conn ["select now()"]))
    (log/info "DB - Connection pool ready")
    ds))


(defn close-datasource [ds]
  (log/info "DB - Closing connection pool")
  (when ds
    (let [pool (jdbc/get-datasource ds)]
      (.close ^HikariDataSource pool)))
  (log/info "DB - Connection pool closed"))
