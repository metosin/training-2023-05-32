(ns training.server.domain.account
  (:require [clojure.tools.logging :as log]
            [ring.util.http-response :as resp]
            [training.server.db.tx :as tx]
            [training.server.db.hug :as hugsql]
            [training.server.api.session :as session]
            [training.server.api.session.middleware :as sm]
            [training.server.http.cache :as cache] 
            [training.server.fx :as fx]))


(hugsql/register-domain :training.server.domain.account)


;;
;; Handlers:
;;


(defn get-fav-by-account-id [req]
  (let [account-id (session/get-session req "id") 
        favs (->> (hugsql/execute! req ::get-fav-by-account-id {:account-id account-id})
                  (mapv :fav/target)) 
        etag (str (hash favs))] 
      (if (= etag (get-in req [:headers cache/value-if-none-match]))
        (resp/not-modified)
        (-> (resp/ok favs)
            (update :headers assoc cache/header-etag etag)))))


(defn update-like [req]
  (let [account-id (session/get-session req "id")
        target-id  (get-in req [:parameters :path :target-id])
        like?      (get-in req [:parameters :body :like])] 
    (hugsql/execute-one! req
                         (if like? ::add-fav ::remove-fav)
                         {:account-id account-id
                          :target-id  target-id}) 
    (resp/ok {})))


(def routes
  ["/account"
   ["/like" {:middleware [sm/require-session-middleware]}
    ["" {:get {:handler #'get-fav-by-account-id}}]
    ["/:target-id" {:middleware [tx/with-tx-middleware]
                    :post       {:parameters {:path [:map
                                                     [:target-id :string]]
                                              :body [:map
                                                     [:like :boolean]]}
                                 :handler    #'update-like}}]]])


;;
;; Fx:
;;


#_
(defn get-fav-by-account-id-fx [req]
  (log/info "get-fav-by-account-id-fx")
  (let [account-id (session/get-session req "id")]
    (-> req
        (fx/resp (resp/ok (hugsql/execute-fx! ::get-fav-by-account-id {:account-id account-id})))
        (fx/post (fn [ctx]
                   (update ctx :body (partial map :fav/target))))
        (fx/post cache/handle-etag))))

#_
(defn update-like-fx [req]
  (log/info "update-like-fx")
  (let [account-id (session/get-session req "id")
        target-id  (get-in req [:parameters :path :target-id])
        like?      (get-in req [:parameters :body :like])]
    (-> req
        (fx/fx (hugsql/execute-one-fx! (if like? ::add-fav ::remove-fav)
                                       {:account-id account-id
                                        :target-id  target-id}))
        (fx/resp (resp/ok {})))))

#_
(def routes
  ["/account"
   ["/like" {:middleware [sm/require-session-middleware]}
    ["" {:get {:handler #'get-fav-by-account-id-fx}}]
    ["/:target-id" {:middleware [tx/with-tx-middleware]
                    :post       {:parameters {:path [:map
                                                     [:target-id :string]]
                                              :body [:map
                                                     [:like :boolean]]}
                                 :handler    #'update-like-fx}}]]])
