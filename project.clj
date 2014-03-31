(defproject comp "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.namespace "0.2.4"]
                 [sqlingvo "0.5.16"]
                 [korma "0.3.0"]
                 [org.clojure/java.jdbc "0.3.3"]
                 [org.postgresql/postgresql "9.2-1002-jdbc4"]
                 [com.mchange/c3p0 "0.9.5-pre6"]
                 [clj-time "0.6.0"]
                 [com.stuartsierra/component "0.2.1"]
                 [compojure "1.1.6"]
                 [ring "1.2.1"]
                 [http-kit "2.1.14"]
                 [clj-http "0.7.7"]
                 [liberator "0.11.0"]
                 [com.taoensso/timbre "3.1.6"]]

  :main comp.core

  :repl-options {:init-ns user})
