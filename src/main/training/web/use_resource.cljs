(ns training.web.use-resource
  (:require [clojure.string :as str]
            [goog.functions :refer [debounce]]
            [helix.hooks :as hooks]
            [promesa.core :as p]
            [training.web.state :as state]
            [training.web.http :as http]))


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


(defn refresh-resource-now [id uri params query]
  (println "refresh-resource-now:" uri (pr-str params) (pr-str query))
  (-> (http/request {:method  :get
                     :uri     (apply-uri-params uri params)
                     :query   query
                     :headers (when-let [etag (get-in @state/app-state [:resource id :etag])]
                                {"if-none-match" etag})})
      (p/then (fn [resp]
                (println "refresh-resource-now: status =" (:status resp))
                (swap! state/app-state update-in [:resource id] merge
                       (case (:status resp)
                         200 {:status :ok
                              :etag   (get-in resp [:headers "etag"])
                              :body   (:body resp)
                              :error  nil}
                         304 {:status :ok
                              :error  nil}
                         {:status :error
                          :etag   nil
                          :body   nil
                          :error  {:status (:status resp)}}))))
      (p/catch (fn [e]
                 (swap! state/app-state update-in [:resource id] merge
                        {:status :error
                         :etag   nil
                         :body   nil
                         :error  {:exception e}})))))


(def doherty-threshold 300)
(def refresh-resource (debounce refresh-resource-now doherty-threshold))


(defn use-resource
  ([id uri] (use-resource id uri nil))
  ([id uri {:keys [params query]}]
   (let [[value set-value] (-> (swap! state/app-state update-in [:resource id]
                                      (fn [resource]
                                        (cond
                                          ; Nothing has changed, use exising resource:
                                          (and (= (:uri resource) uri)
                                               (= (:params resource) params)
                                               (= (:query resource) query))
                                          resource

                                          ; Using same resource but different query params, initiate refresh,
                                          ; save query, but use existing data until refresh arrives:
                                          (and (= (:uri resource) uri)
                                               (= (:params resource) params)
                                               (not= (:query resource) query))
                                          (do (refresh-resource id uri params query)
                                              (merge resource {:status :refreshing
                                                               :query  query}))

                                          ; Resource uri has changed, start from scratch, don't reuse data:
                                          :else
                                          {:status :pending
                                           :id     id
                                           :uri    uri
                                           :query  query
                                           :params params})))
                               (get-in [:resource id])
                               (hooks/use-state))]
     (hooks/use-effect
      :once
      (let [key (gensym)]
        (add-watch state/app-state key (fn [_ _ old-atom-value new-atom-value]
                                         (let [old-resource (-> old-atom-value :resource id)
                                               new-resource (-> new-atom-value :resource id)]
                                           (when (not= old-resource new-resource)
                                             (set-value new-resource)))))
        (refresh-resource-now id uri params query)
        (fn [] (remove-watch state/app-state key))))
     value)))
