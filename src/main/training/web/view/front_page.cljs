(ns training.web.view.front-page
  (:require [helix.core :as hx :refer [defnc <>]]
            [helix.dom :as d]))


(defnc FrontPage [_]
  (d/div {:style {:display     "flex"
                  :flex-flow   "column"
                  :align-items "center"
                  :gap         "2em"
                  :margin-top  "4em"}}
         (d/img {:src "/image/epes-header.jpeg"})
         (d/p {:style {:color "gray"}}
              "In memoriam - "
              (d/a {:href   "http://www.epes.fi/"
                    :target "_blank"}
                   "Epe's"))))
