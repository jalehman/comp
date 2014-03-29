(defproject comp "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.namespace "0.2.4"]
                 [korma "0.3.0"]
                 [org.postgresql/postgresql "9.2-1002-jdbc4"]
                 [com.stuartsierra/component "0.2.1"]
                 [compojure "1.1.6"]
                 [ring "1.2.1"]
                 [http-kit "2.1.14"]
                 [clj-http "0.7.7"]
                 [liberator "0.11.0"]]

  :main comp.core)
