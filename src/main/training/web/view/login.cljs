(ns training.web.view.login
  (:require [helix.core :as hx :refer [defnc $]]
            [helix.dom :as d]
            [helix.hooks :as hooks]
            [training.web.session :as session]
            [promesa.core :as p]
            [clojure.string :as str]))


(defnc LoginView [_]
  (let [[data set-data]   (hooks/use-state {:user     ""
                                            :password ""})
        [state set-state] (hooks/use-state :ready)]
    (d/form
     {:on-submit (fn [e]
                   (.preventDefault e)
                   (set-state :pending)
                   (-> (session/login data)
                       (p/then (fn [{:keys [success? message]}]
                                 (set-state (if success? :success :fail))
                                 (println "success" success? ":" message)))))}
     (d/div
      {:class "grid"}
      (d/label
       {:for "user"}
       "User:"
       (d/input {:id           "user"
                 :name         "user"
                 :type         "text"
                 :placeholder  "Username"
                 :required     true
                 :value        (:user data)
                 :on-change    (fn [e] (set-data assoc :user (-> e .-target .-value)))
                 :aria-invalid (if (= state :fail) true nil)}))
      (d/label
       {:for "password"}
       "Password:"
       (d/input {:id           "password"
                 :name         "password"
                 :type         "password"
                 :placeholder  "Password"
                 :required     true
                 :value        (:password data)
                 :on-change    (fn [e] (set-data assoc :password (-> e .-target .-value)))
                 :aria-invalid (if (= state :fail) true nil)})))
     (d/button {:type      "submit"
                :aria-busy (= state :pending)
                :disabled  (or (str/blank? (:user data))
                               (str/blank? (:password data)))}
               "Login"))))
