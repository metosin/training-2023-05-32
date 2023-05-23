(ns training.web.view.main-view
  (:require [helix.core :as hx :refer [defnc $]]
            [helix.dom :as d]
            [training.web.util :as util]
            [training.web.routing :as routing]
            [training.web.session :as session]
            [training.web.view.nav-bar :as nav]
            [training.web.view.not-found-page :as not-found]
            [training.web.view.login :as login]))


(defnc MainView [_]
  (let [current-route  (routing/use-route)
        session-status (session/use-session [:status])]
    (d/main
     {:class "container"}
     (d/header
      (d/h1 "Clojure Advanced koututus " ($ util/icon {:name "thumb_up"})))
     (when (= session-status :ok)
       ($ nav/NavBar))
     (case session-status
       nil (d/div "Loading....")
       :error (d/div "Error!")
       :no ($ login/LoginView)
       :ok ($ (-> current-route :data :view (or not-found/NotFoundPage)) {:route current-route}))
     (d/footer
      {:class "copyright"}
      (d/small "Copyright © 2023 "
               (d/a {:href   "https://metosin.fi/"
                     :target "_blank"}
                    "Metosin Ltd"))
      (d/br)
      (d/small "MusicBrainz DB licensed under "
               (d/a {:href   "https://creativecommons.org/publicdomain/zero/1.0/"
                     :target "_blank"}
                    "CC0")
               " by "
               (d/a {:href   "https://musicbrainz.org/doc/MusicBrainz_Database"
                     :target "_blank"}
                    "MusicBrainz.org"))))))
