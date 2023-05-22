(ns training.web.view.not-found-page
  (:require [helix.core :as hx :refer [defnc]]
            [helix.dom :as d]))


(defnc NotFoundPage [_]
  (d/div "404"))
