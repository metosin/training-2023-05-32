(ns training.web.use-resource
  (:require [clojure.string :as str]
            [helix.hooks :as hooks]
            [promesa.core :as p]
            [training.web.state :as state]
            [training.web.http :as http]
            [training.web.util :as u]))


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


(defn refresh-resource! [{:keys [uri params query etag key]
                          :as   resource}]
  (-> (http/request {:method  :get
                     :uri     (apply-uri-params uri params)
                     :query   query
                     :headers (when etag
                                {"if-none-match" etag})})
      (p/then (fn [resp]
                (swap! state/app-state update-in [:resource key] merge
                       (case (:status resp)
                         200 {:status    :ok
                              :etag      (get-in resp [:headers "etag"])
                              :body      (:body resp)
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
                          :ttl       nil}))))
      (p/catch (fn [e]
                 (swap! state/app-state update-in [:resource key] merge
                        {:status    :error
                         :etag      nil
                         :body      nil
                         :error     {:exception e}
                         :refreshed (u/now)}))))
  resource)


(defn use-resource
  ([id uri] (use-resource id uri nil))
  ([id uri {:keys [params query]}]
   (let [now                     (u/now)
         [resource set-resource] (-> (swap! state/app-state update-in [:resource id]
                                            (fn [resource]
                                              (cond
                                                ; New reqource request:
                                                (nil? resource)
                                                (let [resource {:key      id
                                                                :uri      uri
                                                                :params   params
                                                                :query    query
                                                                :status   :pending
                                                                :created  (u/now)
                                                                :ttl      0
                                                                :refcount 0}]
                                                  (refresh-resource! resource))

                                                ; Coordinates changed:
                                                (not (and (= uri (:uri resource))
                                                          (= params (:params resource))
                                                          (= query (:query resource))))
                                                (-> (assoc resource
                                                           :status :refreshing
                                                           :uri    uri
                                                           :params params
                                                           :query  query
                                                           :ttl    0)
                                                    (refresh-resource!))

                                                ; TTL expired and not refreshed in 10secs
                                                (and (> now (:ttl resource))
                                                     (> now (+ (:refreshed resource) 10000)))
                                                (-> (assoc resource :status :refreshing)
                                                    (refresh-resource!))

                                                :else
                                                resource)))
                                     (get-in [:resource id])
                                     (hooks/use-state))]
     (hooks/use-effect
      :once
      (do (swap! state/app-state update-in [:resource id :refcount] inc)
          (add-watch state/app-state id (fn [_ _ old-atom-value new-atom-value]
                                          (let [old-resource (get-in old-atom-value [:resource id])
                                                new-resource (get-in new-atom-value [:resource id])]
                                            (when (or (not= (:status old-resource)
                                                            (:status new-resource))
                                                      (not= (:etag old-resource)
                                                            (:etag new-resource))
                                                      (not= (:body old-resource)
                                                            (:body new-resource)))
                                              (set-resource new-resource)))))
          (fn []
            (remove-watch state/app-state id)
            (swap! state/app-state update-in [:resource id :refcount] dec))))
     resource)))
