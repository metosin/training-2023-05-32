(ns training.server.domain.account
  (:require [training.server.db.honey :as sql]))


(defn get-account-by-id [ctx account-id]
  (sql/execute-one! ctx {:select [:account/id :account/username :account/fullname :account/role]
                         :from   [:epes.account]
                         :where  [:= :account/id account-id]}))


(defn find-account-by-username-password [ctx username password]
  (sql/execute-one! ctx {:select [:account/id :account/username :account/fullname :account/role]
                         :from   [:epes.account]
                         :where  [:and
                                  [:= :account/username username]
                                  [:= :account/password [:crypt password :account/password]]]}))


(defn find-account-by-apikey [ctx apikey]
  (sql/execute-one! ctx {:select [:account/id :account/username :account/fullname :account/role]
                         :from   [:epes.account]
                         :join   [:epes.apikey [:= :apikey/account :account/id]]
                         :where  [:and
                                  [:= :apikey/apikey apikey]]}))


(comment

  (let [ctx    {:system {:ds (:ds user/system)}}
        apikey "james"]
    (find-account-by-apikey ctx apikey))

  ;
  )