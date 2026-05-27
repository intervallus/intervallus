(ns intervallus.rotas
  (:require [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]
            [intervallus.calculos :as calc]))

(defroutes app-routes
  (GET "/api/trastes" [escala trastes]
    (let [L (try (Double/parseDouble escala) (catch Exception _ nil))
          n (or (try (Integer/parseInt trastes) (catch Exception _ nil)) 22)]
      (if (and L (>= L 400) (<= L 1000))
        {:status 200 :body {:escala-mm L :trastes (calc/calcular L n)}}
        {:status 400 :body {:erro "Informe 'escala' entre 400 e 1000 mm"}})))
  (route/not-found {:status 404 :body {:erro "Rota não encontrada"}}))  