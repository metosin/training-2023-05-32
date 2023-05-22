(ns training.server.system
  (:require [clojure.tools.logging :as log]
            [training.server.config :as config]
            [training.server.http.server :as http]
            [training.server.db.ds :as ds]
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
        system  {:ds ds}
        handler (handler/handler (:api config) #'routes/routes system)
        http    (http/start-server (:http config) handler)]
    (log/info "System up!")
    {:ds   ds
     :http http}))


#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn stop-system [system]
  (when-let [{:keys [ds http]} system]
    (log/info "Stopping system...")
    (http/stop-server http)
    (ds/close-datasource ds)
    (log/info "System stopped!")))
