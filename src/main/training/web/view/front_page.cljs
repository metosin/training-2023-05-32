(ns training.web.view.front-page
  (:require [helix.core :as hx :refer [defnc]]
            [helix.dom :as d]))


(defnc FrontPage [_]
  (d/div {:style {:display     "flex"
                  :flex-flow   "column"
                  :align-items "center"
                  :gap         "1em"
                  :margin-top  "4em"}}
         (d/img {:src "/image/epes-header.jpeg"})
         (d/p {:style {:color   "gray"
                       :display "flex"
                       :gap     "1em"
                       :margin  0}}
              (d/span "★ 1972")
              (d/span "† 2014"))
         (d/p {:style {:color  "gray"
                       :margin 0}}
              "In memoriam - "
              (d/a {:href   "http://www.epes.fi/"
                    :target "_blank"}
                   "Epe's"))))
