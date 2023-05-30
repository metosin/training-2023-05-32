(ns training.server.http.server
  (:require [clojure.tools.logging :as log]
            [ring.adapter.jetty :as jetty]
            [s-exp.mina :as mina])
  (:import (org.eclipse.jetty.server Server)))


(defn start-server [{:keys [host port]} handler]
  (let [server (jetty/run-jetty handler {:host  host
                                         :port  port
                                         :join? false})]
    (log/infof "Jetty HTTP server listening at http://%s:%d" host port)
    server))


(defn stop-server [^Server server]
  (when server
    (.stop server)
    (log/info "Jetty HTTP server stopped")))



(comment

  ; https://helidon.io/nima
  ; https://medium.com/helidon/helidon-n%C3%ADma-helidon-on-virtual-threads-130bb2ea2088#f3b5

  (defn start-server [{:keys [host port]} handler]
    (let [server (mina/start! handler {:host host
                                       :port port})]
      (log/infof "Nima HTTP server listening at http://%s:%d" host port)
      server))

  (defn stop-server [server]
    (when server
      (mina/stop! server)
      (log/info "Nima HTTP server stopped")))
  ;
  )
