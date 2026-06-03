(ns intervallus.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [compojure.core :refer [defroutes]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.middleware.params :refer [wrap-params]]
            [intervallus.rotas :refer [app-routes]])
  (:gen-class))

(def app
 (-> app-routes
      wrap-params))

(defn -main []
  (println "Servidor na porta 3000")  
  (run-jetty app {:port 3000 :join? true}))