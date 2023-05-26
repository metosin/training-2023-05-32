(ns training.web.util
  (:require [helix.core :as hx :refer [defnc]]
            [helix.dom :as d]))


(defnc icon [{:keys [name]}]
  (d/span {:class "material-symbols-outlined"}
          name))


(defn now []
  (js/Date.now))
