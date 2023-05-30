(ns training.db-fixture
  (:require [clojure.string :as str]
            [next.jdbc :as jdbc]
            [next.jdbc.connection])
  (:import (org.flywaydb.core Flyway)
           (com.zaxxer.hikari HikariDataSource)))


(defn- strings ^"[Ljava.lang.String;" [& args]
  (into-array String args))


(defn flyway ^Flyway [db-name]
  (-> (Flyway/configure)
      (.dataSource (str "jdbc:postgresql://localhost:5432/" db-name) "postgres" "postgres")
      (.locations (strings "migration/musicbrainz" "migration/sql"))
      (.schemas (strings "mb" "epes"))
      (.cleanDisabled false)
      (.load)))


(defn migrate [flyway]
  (let [result (.migrate flyway)]
    (when-not (.success result)
      (throw (ex-info "DB migration failed" {})))
    result))


(defn info [flyway]
  (->> (.info flyway)
       (.applied)
       (mapv (fn [mi]
               [(.getDisplayName (.getState mi)) (.getPhysicalLocation mi)]))))


(defn clean [flyway]
  (let [result (.clean flyway)]
    {:cleaned (.-schemasCleaned result)
     :dropped (.-schemasDropped result)}))


(defn get-admin-connection []
  (jdbc/get-connection {:dbtype "postgres"
                        :host   "localhost"
                        :port   5432
                        :dbname "musicbrainz"}
                       {:user        "postgres"
                        :password    "postgres"
                        :auto-commit true}))


(defn create-db [db-name]
  (with-open [conn (get-admin-connection)]
    (jdbc/execute-one! conn [(str "create database " db-name " with lc_ctype='C.UTF-8'")])))


(defn drop-db [db-name]
  (with-open [conn (get-admin-connection)]
    (jdbc/execute-one! conn [(str "drop database " db-name)])))


(def ^:dynamic *test-db-name* nil)
(def ^:dynamic *test-db-ds* nil)


(defn with-test-db [f]
  (let [test-db-name (str "test_db_" (System/currentTimeMillis))
        cleanup      (atom [])]
    (try
      (create-db test-db-name)
      (swap! cleanup conj (fn [] (drop-db test-db-name)))
      (let [ds (next.jdbc.connection/->pool HikariDataSource {:dbtype   "postgres"
                                                              :host     "localhost"
                                                              :port     5432
                                                              :dbname   test-db-name
                                                              :username "postgres"
                                                              :password "postgres"})]
        (swap! cleanup conj (fn [] (.close ds)))
        (binding [*test-db-name* test-db-name
                  *test-db-ds*   ds]
          (f)))
      (finally
        (future
          (doseq [clean @cleanup]
            (try
              (clean)
              (catch Exception e
                (println "WARNING: Error while running cleanup on" test-db-name)
                (println e)))))))))


(defn with-test-fixture [f]
  (migrate (flyway *test-db-name*))
  (f))



(comment

  (def db-name "test_1")

  (create-db "test_1")
  (def f (flyway db-name))
  (migrate f)
  (info f)

  (with-open [conn (jdbc/get-connection {:dbtype "postgres"
                                         :host   "localhost"
                                         :port   5432
                                         :dbname "musicbrainz"}
                                        {:user     "postgres"
                                         :password "postgres"})]
    (doseq [artist-iname ["rammstein" "abba"]]
      (println "INSERT INTO mb.artist (id, name, disambiguation) VALUES")
      (let [{:artist/keys [id name disambiguation]} (jdbc/execute-one! conn ["select * from mb.artist where iname = ?" artist-iname])]
        (printf "   ('%s', '%s', '%s');\n\n"
                id
                (str/replace name "'" "\\'")
                disambiguation)
        (print "INSERT INTO mb.album (id, name, artist, released) VALUES")
        (->> (jdbc/execute! conn ["select * from mb.album where artist = ?" id])
             (map (fn [{:album/keys [id name artist released]}]
                    (format "  ('%s', '%s', '%s', '%s'::date)"
                            id
                            (str/replace name "'" "\\'")
                            artist
                            released)))
             (str/join ",\n")
             (println))
        (println ";"))
      (println "\n")))

  (clean f)
  (drop-db "test_1")

  ;
  )


