(ns comp.server
  (:require [comp.models.recipe :as recipe]
            [compojure.handler :as handler]
            [compojure.core :refer [GET POST PUT ANY defroutes context]]
            [org.httpkit.server :refer [run-server]]
            [com.stuartsierra.component :as component]
            [ring.middleware.reload :as reload]))

(defn one-recipe-handler
  [req]
  (ring.util.response/response "Testing testing..."))

(defroutes app-routes
  (GET "/" req (one-recipe-handler req)))

(defn wrap-app-component [f web-app]
  (fn [req]
    (f (assoc req ::web-app web-app))))

(defn make-handler [web-app]
  (-> app-routes
      (wrap-app-component web-app)
      (handler/site)
      (reload/wrap-reload)))

(defrecord Server [web-app port]
  component/Lifecycle
  (start [component]
    (println ";; Starting HTTP-Kit server on port" port)
    (assoc component :server
           (run-server (make-handler web-app) {:port port})))
  (stop [component]
    (println ";; Shutting down HTTP-Kit server")
    (let [server (:server component)]
      (server)
      component)))

(defn server [port]
  (component/using (map->Server {:port port}) [:web-app]))
