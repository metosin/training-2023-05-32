(ns training.web.view.about-page
  (:require [helix.core :as hx :refer [defnc]]
            [helix.dom :as d]
            [training.web.http :as http]))


(defnc AboutPage [_]
  (d/div "About page"))
