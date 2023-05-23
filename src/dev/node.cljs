(ns node
  (:require [clojure.string :as str]))


(defn hello []
  "hi, node")


(comment

  (require '[training.web.state :as state])

  (-> @state/app-state
      :resource
      :training.web.view.artist-page/artist
      :body)






  ;
  )
