(ns training.web.view.front-page
  (:require [helix.core :as hx :refer [defnc]]
            [helix.dom :as d]))


(defnc FrontPage [_]
  (d/div "Front page"))
