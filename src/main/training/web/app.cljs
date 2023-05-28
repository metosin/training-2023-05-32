(ns training.web.app
  (:require ["react-dom/client" :refer [createRoot]]
            [helix.core :as hx :refer [$]]
            [applied-science.js-interop :as j]
            [training.web.state :as state]
            [training.web.session :as session]
            [training.web.routing :as routing]
            [training.web.view.main-view :as main]))


(goog-define DEV false)


#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn stop []
  (js/console.log "Stopping..."))


(defonce root (-> (js/document.getElementById "app")
                  (createRoot)))


(defn ^:export start []
  (js/console.log "Starting in" (if DEV "DEV" "PRODUCTION") "mode...")
  (swap! state/app-state assoc ::mode (if DEV :dev :prod))
  (session/init!)
  (routing/init!)
  (j/call root :render ($ main/MainView)))

