(ns training.web.view.item-page
  (:require [helix.core :as hx :refer [defnc <>]]
            [helix.dom :as d]))


(defnc ItemPage [{:keys [route]}]
  (<>
   (d/div "Item page")
   (d/div "item ID: " (-> route :parameters :path :id))))
