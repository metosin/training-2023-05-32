(ns node
  (:require [clojure.string :as str]))


(defn hello []
  "hi, node")


(comment

  (require '[training.web.state :as state])

  (-> @state/app-state
      :resource
      :training.web.view.artist-page/favs
      :body)

  (swap! state/app-state update-in
         [:resource
          :training.web.view.artist-page/favs
          :body]
         disj
         "e464e167-83ab-3b59-88bd-262cf552056e")

  ;
  )
