(ns training.server.http.handler
  (:require  [clojure.string :as str]
             [clojure.tools.logging :as log]
             [muuntaja.core]
             [jsonista.core :as json]
             [ring.util.http-response :as resp]
             [ring.middleware.params]
             [ring.middleware.cookies]
             [reitit.ring :as ring]
             [reitit.ring.coercion :as rrc]
             [reitit.coercion.malli]
             [reitit.ring.middleware.muuntaja]
             [reitit.ring.middleware.exception :as exception]))


(defn- wrap-system [handler system]
  (fn [req]
    (handler (assoc req :system system))))


(defn- exception-handler [handler e req]
  (when-not (-> e (ex-data) :type (= ::resp/response))
    (log/errorf e "error: %s %s"
                (-> req :request-method (name) (str/upper-case))
                (-> req :uri)))
  (handler e req))



(defn- make-handler [routes system]
  (ring/ring-handler
   (ring/router routes
                {:data {:muuntaja   muuntaja.core/instance
                        :coercion   reitit.coercion.malli/coercion
                        :middleware [ring.middleware.params/wrap-params
                                     ring.middleware.cookies/wrap-cookies
                                     reitit.ring.middleware.muuntaja/format-middleware
                                     rrc/coerce-exceptions-middleware
                                     rrc/coerce-request-middleware
                                     rrc/coerce-response-middleware
                                     (exception/create-exception-middleware (assoc exception/default-handlers
                                                                                   ::exception/wrap
                                                                                   exception-handler))
                                     [wrap-system system]]}})
   (constantly (-> {:type    :error
                    :message "route not found"}
                   (json/write-value-as-string)
                   (resp/not-found)
                   (update :headers assoc "content-type" "application/json")))))


(defn handler [{:keys [mode]} routes-var system]
  (case mode
    :dev (do (log/warn "creating reloading ring handler")
             (ring/reloading-ring-handler (fn [] (make-handler @routes-var system))))
    :prod (do (log/info "creating ring handler")
              (make-handler @routes-var system))))
