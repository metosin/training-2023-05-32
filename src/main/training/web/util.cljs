(ns training.web.util
  (:require [helix.core :as hx :refer [defnc]]
            [helix.dom :as d]))


(defnc icon [{:keys [name class style]}]
  (let [class (into ["material-symbols-outlined" "icon"] (if (sequential? class)
                                                           class
                                                           [class]))]
    (d/span {:class class
             :style style}
            name)))


(defn now []
  (js/Date.now))
