(ns training.server.domain.account
  (:require [ring.util.http-response :as resp]
            [training.server.db.tx :as tx]
            [training.server.db.hug :as hugsql]
            [training.server.api.session :as session]
            [training.server.api.session.middleware :as sm]))


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
  (let [account-id (get-account-id req)]
    ; TODO: assert calling user is same as account-id, OR caller is admin
    (->> (hugsql/execute! req ::get-fav-by-account-id {:account-id account-id})
         (map :fav/target)
         (resp/ok))))


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
