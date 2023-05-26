(ns training.server.http.cache)


(def ^:const cache-control "cache-control")
(def ^:const no-store "no-store")
(def ^:const no-cache "no-cache")
(def ^:const etag "etag")
(def ^:const if-none-match "if-none-match")


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
        (if-not (contains? headers cache-control)
          (update resp :headers assoc cache-control
                  (if (or (contains? headers etag)
                          (= (:status resp) 304))
                    no-cache
                    no-store))
          resp)))))
