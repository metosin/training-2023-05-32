(ns training.web.view.nav-bar
  (:require [helix.core :as hx :refer [defnc]]
            [helix.dom :as d]
            [reitit.core :as reitit]
            [reitit.frontend.easy :as rfe]
            [training.web.routing :as router]
            [training.web.session :as session]))


(def nav-routes
  (->> (reitit/routes router/router)
       (map second)
       (filter :nav)
       (sort-by (comp :index :nav))))


(defnc NavBar [_]
  (let [user (session/use-session [:user])]
    (d/nav
     (d/ul
      (for [{:keys [name data nav]} nav-routes]
        (d/li {:key name} (d/a {:href (rfe/href name data)} (:name nav)))))
     (when user
       (d/ul
        (d/li
         (d/details
          {:role "list"}
          (d/summary {:aria-haspopup "listbox"}
                     (get user "fullname"))
          (d/ul {:role "listbox"}
                (d/li (d/a {:on-click session/logout} "Logout"))))))))))
