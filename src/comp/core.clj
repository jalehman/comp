(ns comp.core
  (:gen-class)
  (:require [comp.server :refer [server]]
            [comp.database :refer [database]]
            [comp.models.recipe :refer [recipe]]
            [com.stuartsierra.component :as component]))

(defn comp-system [config]
  (let [{:keys [port]} config]
    (component/system-map
     :server (server port)
     :database (database "fitsme" "postgres" "waf3Wawe")
     :recipe (component/using (recipe) [:database])
     :web-app (component/using (recipe) [:database]))))


(defn -main [& args]
  (component/start
   (comp-system {:port 8085})))
