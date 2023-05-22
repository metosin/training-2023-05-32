(ns training.server.main
  (:require [training.server.system :as system]))


(defn -main [& _args]
  (system/start-system))
