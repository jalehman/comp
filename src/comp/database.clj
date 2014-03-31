(ns comp.database
  (:require [clojure.java.jdbc :as j]
            [sqlingvo.core :refer [sql]]
            [com.stuartsierra.component :as component])
  (:import com.mchange.v2.c3p0.ComboPooledDataSource))

;; =============================================================================
;; Util

(defn- get-connection
  [conn]
  (if (delay? conn)
    @conn conn))

(defmacro query [conn stmt]
  `(j/query ~(get-connection conn)
            (sql (sqlingvo.vendor/->postgresql)
                 ~stmt)))

(defmacro execute! [conn stmt]
  `(j/execute! ~(get-connection conn)
               (sql (sqlingvo.vendor/->postgresql)
                    ~stmt)))

;; =============================================================================
;;

(defn db-spec
  [{:keys [db user password port host]
    :or   {host "localhost" port 5432}}]
  {:classname "com.postgresql.Driver"
   :subprotocol "postgresql"
   :subname (format "//%s:%d/%s" host port db)
   :user user
   :password password})

(defn connection-pool
  "Create a connection pool for the given database spec. Taken from Korma src."
  [{:keys [subprotocol subname classname user password
           excess-timeout idle-timeout minimum-pool-size maximum-pool-size
           test-connection-query
           idle-connection-test-period
           test-connection-on-checkin
           test-connection-on-checkout]
    :or {excess-timeout (* 30 60)
         idle-timeout (* 3 60 60)
         minimum-pool-size 3
         maximum-pool-size 15
         test-connection-query nil
         idle-connection-test-period 0
         test-connection-on-checkin false
         test-connection-on-checkout false}
    :as spec}]
  {:datasource (doto (ComboPooledDataSource.)
                 (.setDriverClass classname)
                 (.setJdbcUrl (str "jdbc:" subprotocol ":" subname))
                 (.setUser user)
                 (.setPassword password)
                 (.setMaxIdleTimeExcessConnections excess-timeout)
                 (.setMaxIdleTime idle-timeout)
                 (.setMinPoolSize minimum-pool-size)
                 (.setMaxPoolSize maximum-pool-size)
                 (.setIdleConnectionTestPeriod idle-connection-test-period)
                 (.setTestConnectionOnCheckin test-connection-on-checkin)
                 (.setTestConnectionOnCheckout test-connection-on-checkout)
                 (.setPreferredTestQuery test-connection-query))})

(defrecord Database [db user pass]
  component/Lifecycle
  (start [component]
    (println ";; Connecting to PostgreSQL database...")
    (let [connection (-> {:db db :user user :password pass}
                         db-spec connection-pool)]
      (assoc component :connection connection)))
  (stop [component]
    (do (println ";; Closing connection to PostgreSQL database...")
        (.close (get-in component [:connection :datasource]))
        (dissoc component :connection))))

(defn database [db user pass]
  (map->Database {:db db :user user :pass pass}))
