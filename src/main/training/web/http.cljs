(ns training.web.http
  (:require [clojure.string :as str]
            [cognitect.transit :as t]
            [promesa.core :as p]
            [applied-science.js-interop :as j]))


(def w (t/writer :json))
(def r (t/reader :json))


(defn write-transit [data]
  (t/write w data))


(defn read-transit [data]
  (t/read r data))


(def transit-content-type "application/transit+json")


(defn- headers->map [^js/Headers headers]
  (->> (j/call headers :entries)
       (map (fn [[k v]] [k v]))
       (into {})))


(defn- query-string [{query :query}]
  (when (seq query)
    (str "?" (->> query
                  (map (fn [[k v]]
                         (str (js/encodeURIComponent (name k)) "=" (js/encodeURIComponent (str v)))))
                  (str/join "&")))))


(defn- http-error!
  ([resp opts] (http-error! resp opts nil))
  ([^js resp {:keys [method uri query]} message]
   (throw (doto (js/Error.)
            (j/assoc! :message (or message (str "HTTP " method " " uri " -> " (j/get resp :status))))
            (j/assoc! :type "http-error")
            (j/assoc! :status (j/get resp :status))
            (j/assoc! :uri uri)
            (j/assoc! :method method)
            (j/assoc! :query query)))))


(defn fetch-opts [opts]
  (j/obj :method (-> opts :method (name) (str/upper-case))
         :headers (clj->js (cond-> (assoc opts "accept" transit-content-type)
                             (:body opts) (assoc "content-type" transit-content-type)
                             (:etag opts) (assoc "if-none-match" (:etag opts))))
         :mode "same-origin"
         :credentials "same-origin"
         :redirect (-> opts :redirect (or :follow) (name))
         :body (when-let [body (:body opts)]
                 (write-transit body))))


(defn request [opts]
  (-> (js/fetch (str (:uri opts) (query-string opts))
                (fetch-opts opts))
      (p/then (fn [^js resp]
                (let [headers      (headers->map (j/get resp :headers))
                      content-type (get headers "content-type" "")]
                  (if (str/starts-with? content-type transit-content-type)
                    (-> (j/call resp :text) 
                        (p/then read-transit) 
                        (p/then (fn [body] 
                                  {:status  (j/get resp :status) 
                                   :headers headers 
                                   :body    body ::request opts})))
                    (do (js/console.warn "unsupported content-type:" 
                                         content-type 
                                         "from"
                                         (-> opts :method (name) (str/upper-case))
                                         (-> opts :uri))
                        (-> (j/call resp :text) 
                        (p/then (fn [body] 
                                  {:status  (j/get resp :status) 
                                   :headers headers 
                                   :body    body ::request opts}))))))))))


#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn POST
  ([uri] (POST uri nil nil))
  ([uri body] (POST uri nil body))
  ([uri query body]
   (request {:uri    uri
             :method :post
             :query  query
             :body   body})))


#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn GET
  ([uri] (GET uri nil))
  ([uri query]
   (request {:uri    uri
             :method :get
             :query  query})))
