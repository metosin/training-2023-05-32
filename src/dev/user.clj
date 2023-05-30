(ns user
  (:require [clojure.tools.namespace.repl :as tnr]
            [clojure.tools.logging :as log]
            [kaocha.repl :as k]
            [zprint.core :as zprint]
            [io.aviso.repl :as aviso]))


(aviso/install-pretty-exceptions)


(defonce system nil)


#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn start []
  (log/info "user/start: system starting...")
  (alter-var-root #'system (constantly ((requiring-resolve 'training.server.system/start-system))))
  (log/info "user/start: system up")
  "System up")


#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn stop []
  (log/info "user/start: system stopping...")
  ((requiring-resolve 'training.server.system/stop-system) system)
  (log/info "user/start: system down")
  "System down")


#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn reset []
  (log/info "user/reset: system reseting...")
  (stop)
  (tnr/refresh :after 'user/start))


#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn run-unit-tests []
  (k/run :unit))


#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn run-all-tests []
  (run-unit-tests))


#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(def zp zprint/czprint)


(comment

  (stop)
  (require '[s-exp.mina :as mina])
  (def nima (mina/start! (fn [req]
                           (log/info "REQ:" (keys req))
                           {:status 200
                            :body   "hello"})
                         {:port 8888}))
  (mina/stop! nima)

  ;
  )