(ns training.server.system
  (:require [clojure.tools.logging :as log]
            [training.server.config :as config]
            [training.server.http.server :as http]
            [training.server.db.ds :as ds]
            [training.server.redis.pool :as redis]
            [training.server.http.handler :as handler]
            [training.server.api.routes :as routes])
  (:import (org.slf4j.bridge SLF4JBridgeHandler)))


#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn start-system []
  (System/setProperty "org.jboss.logging.provider" "slf4j")
  (SLF4JBridgeHandler/install)
  (log/infof "Starting system, JDK %s version %s..."
             (System/getProperty "java.vendor")
             (System/getProperty "java.version"))
  (let [config  (config/load-config)
        ds      (ds/make-datasource (:db config))
        redis   (redis/make-pool (:redis config))
        system  {:ds    ds
                 :redis redis}
        handler (handler/make-handler routes/routes system)
        http    (http/start-server (:http config) handler)]
    (log/info "System up!")
    {:ds    ds
     :http  http
     :redis redis}))


#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn stop-system [system]
  (when-let [{:keys [ds http redis]} system]
    (log/info "Stopping system...")
    (http/stop-server http)
    (ds/close-datasource ds)
    (redis/close-pool redis)
    (log/info "System stopped!")))
