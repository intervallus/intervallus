(defproject calculador-trastes "1.0.0"
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [ring/ring-jetty-adapter "1.11.0"]
                 [ring/ring-json "0.5.1"]
                 [compojure "1.7.1"]
                 [hiccup "1.0.5"]
                 [org.clojure/data.json "2.5.0"]]
  :source-paths ["src"]
  :main intervallus.core
  :aot [intervallus.core])