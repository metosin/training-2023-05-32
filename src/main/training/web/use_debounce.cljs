(ns training.web.use-debounce
  (:require [helix.hooks :as hooks]
            [goog.functions :refer [debounce]]))


(def ^:const doherty-threshold 240)


(defn use-debounce
  ([value] (use-debounce value doherty-threshold))
  ([value treshold]
   (let [[value set-value] (hooks/use-state value)
         debounced         (hooks/use-memo :once (debounce set-value treshold))]
     [value debounced])))
