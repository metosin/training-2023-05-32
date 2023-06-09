(ns training.server.http.handler
  (:require  [clojure.string :as str]
             [clojure.tools.logging :as log]
             [clojure.walk]
             [muuntaja.core]
             [jsonista.core :as json]
             [ring.util.http-response :as resp]
             [ring.middleware.params]
             [ring.middleware.cookies]
             [reitit.ring :as ring]
             [reitit.ring.coercion :as rrc]
             [reitit.coercion.malli]
             [reitit.ring.middleware.muuntaja]
             [reitit.ring.middleware.exception :as exception]
             [training.server.http.cache :as cache]
             [training.server.redis.pool :as redis]
             [training.server.api.session.middleware :as session]
             ; FIXME:
             [training.server.fx :as fx]))


(defn- wrap-system [handler system]
  (fn [req]
    (handler (assoc req :system system))))


(defn- exception-handler [handler e req]
  (when-not (-> e (ex-data) :type (= ::resp/response))
    (log/errorf e "error: %s %s"
                (-> req :request-method (name) (str/upper-case))
                (-> req :uri)))
  (handler e req))


(defn- deref-var-handlers [routes]
  (clojure.walk/prewalk (fn [v]
                          (if (var? v)
                            (deref v)
                            v))
                        routes))


(defn make-handler [{mode :mode} routes system]
  (ring/ring-handler
   (ring/router (if (= mode :dev)
                  (do (log/warn "make-handler: DEV mode")
                      routes)
                  (do (log/info "make-handler: PROD mode")
                      (deref-var-handlers routes)))
                {:data {:muuntaja   muuntaja.core/instance
                        :coercion   reitit.coercion.malli/coercion
                        :middleware [cache/cache-middleware
                                     ring.middleware.params/wrap-params
                                     ring.middleware.cookies/wrap-cookies
                                     reitit.ring.middleware.muuntaja/format-middleware
                                     rrc/coerce-exceptions-middleware
                                     rrc/coerce-request-middleware
                                     rrc/coerce-response-middleware
                                     (exception/create-exception-middleware (assoc exception/default-handlers
                                                                                   ::exception/wrap
                                                                                   exception-handler))
                                     [wrap-system system]
                                     [redis/redis-middleware]
                                     [session/session-middleware]
                                     [fx/fx-middleware]]}})
   (constantly (-> {:type    :error
                    :message "route not found"}
                   (json/write-value-as-string)
                   (resp/not-found)
                   (update :headers assoc "content-type" "application/json")))))
