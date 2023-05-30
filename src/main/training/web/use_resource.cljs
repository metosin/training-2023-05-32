(ns training.web.use-resource
  (:require [clojure.string :as str]
            [helix.hooks :as hooks]
            [promesa.core :as p]
            [training.web.state :as state]
            [training.web.http :as http]
            [training.web.util :as u]
            [training.web.use-resource :as resource]
            [applied-science.js-interop :as j]))


(defn apply-uri-params [uri params]
  (loop [uri   uri
         parts []]
    (let [[m part] (re-find #"/([^/]+)" uri)]
      (if (nil? m)
        (str "/" (str/join "/" parts) uri)
        (recur (subs uri (count m))
               (conj parts (if (= ":" (subs part 0 1))
                             (get params (keyword (subs part 1)))
                             part)))))))


(def ^:const default-max-age (* 1 60 1000))


(defn ttl [resp]
  (let [now           (u/now)
        cache-control (get-in resp [:headers "cache-control"] "")
        [_ max-age]   (re-find #"max-age=(\d+)" cache-control)]
    (if max-age
      (+ now (* (js/parseInt max-age 10) 1000))
      (+ now default-max-age))))


(defn abort-refresh! [resource]
  (when-let [old-abort-conroller (:abort-conroller resource)]
    (j/call old-abort-conroller :abort))
  resource)


(defn refresh-resource! [{:keys [id status uri params query parser etag]
                          :as   resource}]
  (abort-refresh! resource)
  (let [new-abort-conroller (js/AbortController.)]
    (-> (http/request {:method  :get
                       :uri     (apply-uri-params uri params)
                       :query   query
                       :headers (when etag {"if-none-match" etag})
                       :signal  (j/get new-abort-conroller :signal)})
        (p/then (fn [resp]
                  (swap! state/app-state update-in [:resource id] merge
                         (case (:status resp)
                           200 {:status    :ok
                                :etag      (get-in resp [:headers "etag"])
                                :body      (let [body (:body resp)]
                                             (if parser
                                               (parser body)
                                               body))
                                :error     nil
                                :refreshed (u/now)
                                :ttl       (ttl resp)}
                           304 {:status    :ok
                                :error     nil
                                :refreshed (u/now)
                                :ttl       (ttl resp)}
                           {:status    :error
                            :etag      nil
                            :body      nil
                            :error     {:status (:status resp)}
                            :refreshed (u/now)
                            :ttl       (ttl resp)}))))
        (p/catch (fn [e]
                   (when-not (= (j/get e :message) "AbortError")
                     (swap! state/app-state update-in [:resource id] merge
                            {:status    :error
                             :etag      nil
                             :body      nil
                             :error     {:exception e}
                             :refreshed (u/now)})))))
    (assoc resource
           :abort-controller new-abort-conroller
           :status           (if (= status :pending) :pending :refreshing))))


(defn update-resource [resource id uri opts]
  (let [now (u/now)]
    (cond
      ; New resource:
      (nil? resource)
      (refresh-resource! {:id      id
                          :uri     uri
                          :params  (:params opts)
                          :query   (:query opts)
                          :parser  (:parser opts)
                          :status  :pending
                          :created now
                          :ttl     0})

      ; Resource coordinates changed:
      (or (not= (:uri resource) uri)
          (not= (:params resource) (:params opts))
          (not= (:query resource) (:query opts)))
      (-> (assoc resource
                 :uri    uri
                 :params (:params opts)
                 :query  (:query opts)
                 :ttl    0)
          (refresh-resource!))

      ; TTL expired and not already refrshing:
      (and (> now (:ttl resource))
           (not (#{:pending :refreshing} (:status resource))))
      (refresh-resource! resource)

      ; All good:
      :else
      resource)))


(defn resource-changed? [old-resource new-resource]
  (or (not= (:status old-resource)
            (:status new-resource))
      (not= (:etag old-resource)
            (:etag new-resource))
      (not= (:body old-resource)
            (:body new-resource))))


(defn resource-change-observer [id set-resource]
  (fn [_ _ old-atom-value new-atom-value]
    (let [old-resource (get-in old-atom-value [:resource id])
          new-resource (get-in new-atom-value [:resource id])]
      (when (resource-changed? old-resource new-resource)
        (set-resource new-resource)))))


(defn use-resource
  ([id uri] (use-resource id uri nil))
  ([id uri opts]
   (let [[resource set-resource] (hooks/use-state
                                  (-> (swap! state/app-state update-in [:resource id]
                                             update-resource id uri opts)
                                      (get-in [:resource id])))]
     (hooks/use-effect :once
                       (add-watch state/app-state id (resource-change-observer id set-resource))
                       (fn []
                         (remove-watch state/app-state id)
                         (abort-refresh! (get-in state/app-state [:resource id]))))
     resource)))


(defn mutate [resource {:keys [method uri params query body]}]
  (-> (http/request {:method (or method :post)
                     :uri    (apply-uri-params (or uri (:uri resource))
                                               (or params (:uri resource)))
                     :query  (or query (:query resource))
                     :body   body})
      (p/then (fn [resp]
                (when (= 200 (:status resp))
                  (-> (assoc resource :status :refreshing)
                      (refresh-resource!)))))))
