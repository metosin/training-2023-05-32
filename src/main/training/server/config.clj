(ns training.server.config
  (:require [clojure.java.io :as io]
            [aero.core :as aero]))


(defn load-config []
  (-> "config.edn"
      (clojure.java.io/resource)
      (aero/read-config {:profile (case (System/getProperty "mode")
                                    "dev" :dev
                                    "prod" :prod)})))


(comment
  (load-config))