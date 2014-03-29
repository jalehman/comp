(ns comp.database
  (:require [korma.db :refer [postgres create-db get-connection]]
            [com.stuartsierra.component :as component]))

(defrecord Database [db user pass]
  component/Lifecycle
  (start [component]
    (println ";; Connecting to PostgreSQL database...")
    (let [db-spec (create-db (postgres {:db db :user user :password pass}))]
      (assoc component :connection db-spec)))
  (stop [component]
    (println ";; Closing connection to PostgreSQL database...")
    (let [server (:server component)]
      (dissoc component :connection))))

(defn database [db user pass]
  (map->Database {:db db :user user :pass pass}))
