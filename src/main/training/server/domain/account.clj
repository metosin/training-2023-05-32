(ns training.server.domain.account
  (:require [ring.util.http-response :as resp]
            [training.server.db.tx :as tx]
            [training.server.db.hug :as hugsql]
            [training.server.api.session :as session]
            [training.server.api.session.middleware :as sm]
            [training.server.http.cache :as cache]
            [training.server.util :as util]))


(hugsql/register-domain :training.server.domain.account)


(defn get-account-id [req] 
  (let [account-id (get-in req [:parameters :path :account-id])]
    (if (= account-id "self")
      (get (::session/session req) "id")
      account-id)))


;;
;; Handlers:
;;


(defn get-fav-by-account-id [req]
  ; TODO: assert calling user is same as account-id, OR caller is admin
  (let [account-id (get-account-id req) 
        checksum   (util/checksummer) 
        favs (->> (hugsql/execute! req ::get-fav-by-account-id {:account-id account-id})
                  (map :fav/target)
                  (map checksum)
                  (doall))
          etag (checksum)] 
    (if (= etag (get-in req [:headers cache/if-none-match]))
      (resp/not-modified)
      (-> (resp/ok favs)
          (update :headers assoc cache/etag etag)))))


(defn update-like [req]
  (let [account-id (get-account-id req)
        target-id  (get-in req [:parameters :path :target-id])
        like?      (get-in req [:parameters :body :like])
        n          (hugsql/execute-one! req
                                        (if like? ::add-fav ::remove-fav)
                                        {:account-id account-id
                                         :target-id  target-id})] 
    (println "UPDATE:" (pr-str n))
    (resp/ok {:account-id account-id
              :target-id  target-id
              :like?      like?
              :updated?   (pos? (:next.jdbc/update-count n))})))


(def routes
  ["/:account-id" {:middleware [sm/require-session-middleware]}
   ["/like"
    ["" {:parameters {:path [:map [:account-id :string]]}
         :get        {:handler get-fav-by-account-id}}]
    ["/:target-id" {:middleware [tx/with-tx-middleware]
                    :post       {:parameters {:path [:map
                                                     [:account-id :string]
                                                     [:target-id :string]]
                                              :body [:map
                                                     [:like :boolean]]}
                                 :handler    update-like}}]]])
