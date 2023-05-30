(ns training.web.view.about-page
  (:require [helix.core :as hx :refer [defnc $]]
            [helix.dom :as d]
            ["markdown-it" :as Markdown] 
            [training.web.use-resource :refer [use-resource]]
            [training.web.util :refer [icon]]))


(defonce render-markdown 
  (let [md (Markdown.)]
    (fn [markdown-content]
      (.render md markdown-content))))


(defnc AboutPage [_]
  (let [about (use-resource ::about "/ABOUT.md" {:parser render-markdown})] 
    (d/div {:class ["about-page"]}
           (case (:status about)
             :pending ($ icon {:class "animate-rotate"
                               :name  "sync"})
             :error (d/div "Error!")
             (:ok :refreshing)
             (d/div {:dangerouslySetInnerHTML #js {"__html" (:body about)}})))))
