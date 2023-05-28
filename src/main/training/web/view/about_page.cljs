(ns training.web.view.about-page
  (:require [helix.core :as hx :refer [defnc $]]
            [helix.dom :as d]
            ["markdown-it" :as Markdown]
            [training.web.state :as state]
            [training.web.use-resource :refer [use-resource]]
            [training.web.util :refer [icon]]))


(defonce render-markdown 
  (let [md (Markdown.)]
    (fn [markdown-content]
      (.render md markdown-content))))


(defnc AboutPage [_]
  (let [about-url (if (= (:training.web.app/mode @state/app-state) :dev)
                    "/ABOUT.md"
                    "https://raw.githubusercontent.com/metosin/training-2023-05-32/main/ABOUT.md")
        about     (use-resource ::about about-url {:parser render-markdown})] 
    (d/div {:class ["about-page"]}
           (case (:status about)
             :pending (d/div ($ icon {:class "animate-rotate"
                                      :name  "sync"}))
             :error (d/div "Error!")
             (:ok :refreshing)
             (d/div {:dangerouslySetInnerHTML #js {"__html" (:body about)}})))))
