(ns training.server.fx
  (:refer-clojure :exclude [assert get])
  (:require [clojure.core :as c]
            [ring.util.http-response :as resp]
            [training.server.fn-bang :as fn-bang]))


;;
;; Public API:
;;


(defn assert [ctx f]
  (if f
    (update-in ctx [::fx :assert] (fnil conj #{}) f)
    ctx))


(defn fx [ctx f]
  (if f
    (update-in ctx [::fx :fx] (fnil conj []) f)
    ctx))


(defn post [ctx f]
  (if f
    (update-in ctx [::fx :post] (fnil conj []) f)
    ctx))


(defn resp [ctx http-resp]
  (merge ctx http-resp))


;;
;; Private execute stuff:
;;


(defn- execute-assert! [ctx f]
  (when-not (f ctx)
    (resp/throw! (or (::on-reject (meta f))
                     (resp/forbidden {:message "forbidden"})))))


(defn- execute-asserts! [ctx]
  (doseq [assertion (-> ctx ::fx :assert)]
    (execute-assert! ctx assertion))
  ctx)


(defn- execute-fxs! [ctx key]
  (reduce (fn [ctx f] (f ctx)) 
          ctx
          (-> ctx ::fx key)))


;;
;; Execute!
;;


(defn- execute-fn!-body [ctx]
  (let [body (:body ctx)]
    (if (fn-bang/fn!? body)
      (assoc ctx :body (body ctx))
      ctx)))


(defn execute! [ctx]
  (-> ctx
      (execute-asserts!)
      (execute-fxs! :fx)
      (execute-fn!-body)
      (execute-fxs! :post)))


;;
;; Ring middleware:
;;


(defn fx-middleware [handler]
  (fn [req]
    (when-let [resp (handler req)]
      (-> (assoc resp ::request req)
          (execute!)))))
