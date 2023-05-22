(ns user
  (:require [clojure.tools.namespace.repl :as tnr]
            [clojure.tools.logging :as log]
            [kaocha.repl :as k]
            [statecharts.core :as fsm]))


(def system nil)


#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn start []
  (log/info "user/start: system starting...")
  (alter-var-root #'system (constantly ((requiring-resolve 'training.server.system/start-system))))
  "System up")


#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn reset []
  (log/info "user/reset: system reseting...")
  ((requiring-resolve 'training.server.system/stop-system) system)
  (tnr/refresh :after 'user/start))


#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn run-unit-tests []
  (k/run :unit))


#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn run-all-tests []
  (run-unit-tests))


(comment


  (def machine
    (fsm/machine {:id      :machine
                  :initial :init
                  :context {:foo 42}
                  :states  {:init   {:on {:quote {:actions (fn [state event]
                                                             (println "state:" (pr-str state))
                                                             (println "event:" (pr-str event)))}}}
                            :string {:on {:quote 1}}}}))

  (def service (fsm/service machine))
  (fsm/start service)
  (fsm/send service {:type :quote
                     :fofo 'baba})

  ;
  )
