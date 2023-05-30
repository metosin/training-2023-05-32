(ns training.server.http.cache
  (:require [training.server.fn-bang :refer [defn!]]
            [training.server.fx :as fx]
            [ring.util.http-response :as resp]))


(def ^:const header-cache-control "cache-control")
(def ^:const header-etag "etag")
(def ^:const value-no-store "no-store")
(def ^:const value-no-cache "no-cache")
(def ^:const value-if-none-match "if-none-match")


;;
;; Middleware to set default Cache-Control on responses. If response contains Cache-Control, 
;; does nothing. If not, then sets Cache-Control to default value. The default for responses 
;; with ETag is "no-cache" and responses without ETag "no-store".
;;
;; Note that "no-cache" does not mean that client should not cache responses. For more info
;; on "no-cache" see https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Cache-Control#no-cache
;;


(defn cache-middleware [handler]
  (fn [req]
    (let [resp    (handler req)
          headers (:headers resp)]
      (when resp
        (if-not (contains? headers header-cache-control)
          (update resp :headers assoc header-cache-control
                  (if (or (contains? headers header-etag)
                          (= (:status resp) 304))
                    value-no-cache
                    value-no-store))
          resp)))))



;; FIXME:
(defn! handle-etag [ctx]
  (let [etag (str (hash (:body ctx)))]
    (if (= etag (get-in ctx [::fx/request :headers value-if-none-match]))
      (resp/not-modified)
      (assoc-in ctx [:headers header-etag] etag))))