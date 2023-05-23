(ns training.web.session
  (:require [promesa.core :as p]
            [training.web.state :as state]
            [training.web.http :as http]))


(defn use-session
  ([] (use-session nil))
  ([transformers] (state/use-app-state (concat [:session] transformers))))


(defn set-session! [session]
  (swap! state/app-state assoc :session session))


(defn init! []
  (set-session! nil)
  (-> (http/POST "/api/session")
      (p/then (fn [resp]
                (if (= (:status resp) 200)
                  (set-session! {:status :ok
                                 :user   (-> resp :body :user)})
                  (set-session! {:status :no}))))
      (p/catch (fn [e]
                 (js/console.error e "unexpected error from /api/session")
                 (set-session! {:status :error})))))


(defn login
  ([data] (login nil data))
  ([e data]
   (when e
     (.preventDefault e))
   (-> (http/POST "/api/session/login" data)
       (p/then (fn [resp]
                 (if (= (:status resp) 200)
                   (do (set-session! {:status :ok
                                      :user   (-> resp :body :user)})
                       {:success? true})
                   (do (set-session! {:status :no})
                       {:success? false
                        :message  (-> resp :body :message)})))))))


(defn logout
  ([] (logout nil))
  ([e]
   (when e
     (.preventDefault e))
   (-> (http/POST "/api/session/logout")
       (p/finally (fn [_]
                    (set-session! {:status :no})
                    (set! (.-location js/window) "/"))))))
