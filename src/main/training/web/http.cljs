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


(defn- headers-obj->clj-map [^js/Headers headers]
  (->> (j/call headers :entries)
       (map (fn [[k v]] [k v]))
       (into {})))


(defn- query-string [{query :query}]
  (when (seq query)
    (str "?" (->> query
                  (map (fn [[k v]]
                         (str (js/encodeURIComponent (name k)) 
                              "=" 
                              (js/encodeURIComponent (str v)))))
                  (str/join "&")))))


(defn fetch-opts [opts]
  (j/obj :method (-> opts :method (name) (str/upper-case))
         :headers (clj->js (cond-> (assoc opts "accept" transit-content-type)
                             (:body opts) (assoc "content-type" transit-content-type)
                             (:etag opts) (assoc "if-none-match" (:etag opts))))
         :mode "same-origin"
         :credentials "same-origin"
         :redirect (-> opts :redirect (or :follow) (name))
         :body (when-let [body (:body opts)]
                 (write-transit body))
         :signal (:signal opts)))


(defn request [opts]
  (-> (js/fetch (str (:uri opts) (query-string opts))
                (fetch-opts opts))
      (p/then (fn [^js resp]
                (let [headers      (-> (j/get resp :headers)
                                       (headers-obj->clj-map))
                      content-type (get headers "content-type" "")
                      transit? (str/starts-with? content-type transit-content-type)]
                  (-> (j/call resp :text) 
                      (p/then (fn [body] 
                                {:status  (j/get resp :status) 
                                 :headers headers 
                                 :body    (if transit? 
                                            (read-transit body) 
                                            body) 
                                 ::request opts}))))))))


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
