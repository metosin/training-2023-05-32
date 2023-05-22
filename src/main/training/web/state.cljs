(ns training.web.state
  (:require [training.web.use-atom :refer [use-atom]]))


(defonce app-state (atom nil))


(defn use-app-state
  ([] (use-app-state nil))
  ([transformers]
   (use-atom app-state transformers)))
