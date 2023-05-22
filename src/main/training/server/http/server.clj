(ns training.server.http.server
  (:require [clojure.tools.logging :as log]
            [ring.adapter.jetty :as jetty])
  (:import (org.eclipse.jetty.server Server)))


(defn start-server [{:keys [host port]} handler]
  (let [server (jetty/run-jetty handler {:host  host
                                         :port  port
                                         :join? false})]
    (log/infof "HTTP server listening at http://%s:%d" host port)
    server))


(defn stop-server [^Server server]
  (when server
    (.stop server)
    (log/info "HTTP server stopped")))
