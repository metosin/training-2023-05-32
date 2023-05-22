(ns training.web.view.nav-bar
  (:require [helix.core :as hx :refer [defnc]]
            [helix.dom :as d]
            [reitit.core :as reitit]
            [reitit.frontend.easy :as rfe]
            [training.web.routing :as router]
            [training.web.session :as session]))


(def nav-routes
  (->> (reitit/routes router/router)
       (keep (fn [[_ {:keys [name nav]}]]
               (when nav
                 [nav name])))
       (sort-by (fn [[[index]]] index))
       (map (fn [[[_ label data] id]]
              [label data id]))))


(defnc NavBar [_]
  (let [user (session/use-session [:user])]
    (d/nav
     (d/ul
      (for [[label data id] nav-routes]
        (d/li {:key id} (d/a {:href (rfe/href id data)} label))))
     (when user
       (d/details
        {:role "list"}
        (d/summary
         {:aria-haspopup "listbox"}
         user)
        (d/ul
         {:role "listbox"}
         (d/li (d/a {:on-click session/logout} "Logout"))))))))



;; <details role="list">
;;   <summary aria-haspopup="listbox">Dropdown</summary>
;;   <ul role="listbox">
;;     <li><a>Action</a></li>
;;     <li><a>Another action</a></li>
;;     <li><a>Something else here</a></li>
;;   </ul>
;; </details>
