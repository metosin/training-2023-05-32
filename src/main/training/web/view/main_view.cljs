(ns training.web.view.main-view
  (:require [helix.core :as hx :refer [defnc $]]
            [helix.dom :as d]
            [training.web.routing :as routing]
            [training.web.session :as session]
            [training.web.view.nav-bar :as nav]
            [training.web.view.not-found-page :as not-found]
            [training.web.view.login :as login]
            [training.web.util :refer [icon]]))


(defnc Header [_]
  (let [session-status (session/use-session [:status])]
    (if (= session-status :ok)
      ($ nav/NavBar)
      (d/div))))


(defnc ExtLink [{:keys [href children]}]
  (d/a {:href   href
        :target "_blank"} children))


(defnc Footer [_]
  (d/footer
   (d/div
    (d/small "Copyright © 2023 " ($ ExtLink {:href "https://metosin.fi/"} "Metosin Ltd")))
   (d/div
    (d/small "MusicBrainz DB licensed under "
             ($ ExtLink {:href "https://creativecommons.org/publicdomain/zero/1.0/"} "CC0")
             " by "
             ($ ExtLink {:href "https://musicbrainz.org/doc/MusicBrainz_Database"} "MusicBrainz.org")))))


(defnc Main [_]
  (let [current-route  (routing/use-route)
        session-status (session/use-session [:status])]
    (case session-status
      nil (d/div ($ icon {:class "animate-rotate"
                          :name  "sync"}))
      :error (d/div "Error!")
      :no ($ login/LoginView)
      :ok ($ (-> current-route :data :view (or not-found/NotFoundPage)) {:route current-route}))))


(defnc MainView [_]
  (d/main
   {:class "container"}
   ($ Header)
   ($ Main)
   ($ Footer)))
