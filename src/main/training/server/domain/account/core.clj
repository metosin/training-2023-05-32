(ns training.server.domain.account.core
  (:require [training.server.db.honey :as honeysql]))


(defn get-account-by-id [ctx account-id]
  (honeysql/execute-one! ctx {:select [:account/id :account/username :account/fullname :account/role]
                              :from   [:epes.account]
                              :where  [:= :account/id account-id]}))


(defn find-account-by-username-password [ctx username password]
  (honeysql/execute-one! ctx {:select [:account/id :account/username :account/fullname :account/role]
                              :from   [:epes.account]
                              :where  [:and
                                       [:= :account/username username]
                                       [:= :account/password [:crypt password :account/password]]]}))


(defn find-account-by-apikey [ctx apikey]
  (honeysql/execute-one! ctx {:select [:account/id :account/username :account/fullname :account/role]
                              :from   [:epes.account]
                              :join   [:epes.apikey [:= :apikey/account :account/id]]
                              :where  [:and
                                       [:= :apikey/apikey apikey]]}))
