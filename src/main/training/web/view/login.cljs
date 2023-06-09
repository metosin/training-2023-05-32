(ns training.web.view.login
  (:require [helix.core :as hx :refer [defnc $]]
            [helix.dom :as d]
            [helix.hooks :as hooks]
            [training.web.session :as session]
            [promesa.core :as p]
            [clojure.string :as str]))


(defnc LoginView [_]
  (let [[data set-data]   (hooks/use-state {:username ""
                                            :password ""})
        [state set-state] (hooks/use-state :ready)]
    (d/div
     (d/form
      {:on-submit (fn [e]
                    (.preventDefault e)
                    (set-state :pending)
                    (-> (session/login data)
                        (p/then (fn [{:keys [success? message]}]
                                  (set-state (if success? :success :fail))
                                  (println "success" success? ":" message)))))}
      (d/h3 {:class ["mt-3em"]} "Login:")
      (d/div
       {:class "mt-3em grid"}
       (d/label
        {:for "username"}
        "User:"
        (d/input {:id           "username"
                  :name         "username"
                  :type         "text"
                  :placeholder  "Username"
                  :auto-focus   true
                  :required     true
                  :value        (:username data)
                  :on-change    (fn [e] (set-data assoc :username (-> e .-target .-value)))
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
                 :disabled  (or (str/blank? (:username data))
                                (str/blank? (:password data)))}
                "Login"))
     (d/div
      {:class "login-hint"}
      (d/div  "Hint: try one of these logins:")
      (d/ul (for [user ["tina" "james" "cyndi"]]
              (d/li {:key      user
                     :on-click (fn [_]
                                 (set-data assoc
                                           :username user
                                           :password user))}
                    (d/span user) " / " (d/span user))))))))
