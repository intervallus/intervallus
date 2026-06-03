(ns intervallus.rotas
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [ring.util.response :refer [response content-type]]
            [clojure.string :as str]
            [clojure.data.json :as json]
            [intervallus.calculos :as calc]))

(def frontend-path "../../frontend")
(def template-path (str frontend-path "/index.html"))

(def notas ["C" "C#" "D" "D#" "E" "F" "F#" "G" "G#" "A" "A#" "B"])
(def dot-frets #{3 5 7 9 15 17 19 21})
(def empty-state "<div class=\"empty\"><div class=\"empty-icon\">𝄞</div><p>Informe a escala e clique em Calcular para gerar a tabela de trastes.</p></div>")

(defn- gerar-resultado-html [escala trastes]
  (let [max-esp       (apply max (map :espacamento-mm trastes))
        meio          (first (filter #(= (:traste %) 12) trastes))
        primeiro      (first trastes)]
    (str
      "<div class=\"info-bar\">"
        "<div class=\"stat\"><span class=\"stat-label\">Escala</span><span class=\"stat-value\">" (format "%.1f" escala) "<span>mm</span></span></div>"
        "<div class=\"stat\"><span class=\"stat-label\">Trastes</span><span class=\"stat-value\">" (count trastes) "</span></div>"
        (when meio (str "<div class=\"stat\"><span class=\"stat-label\">Ponto médio (12º)</span><span class=\"stat-value\">" (format "%.1f" (:dist-pestana-mm meio)) "<span>mm</span></span></div>"))
        (when primeiro (str "<div class=\"stat\"><span class=\"stat-label\">1º espaçamento</span><span class=\"stat-value\">" (format "%.1f" (:espacamento-mm primeiro)) "<span>mm</span></span></div>"))
      "</div>"

      "<div class=\"table-wrap\">"
        "<table>"
          "<thead><tr><th>#</th><th>Dist. Pestana (mm)</th><th>Dist. Ponte (mm)</th><th>Espaçamento (mm)</th><th>Nota</th><th class=\"bar-cell\">Espaç. visual</th></tr></thead>"
          "<tbody>"
            (str/join
              (for [t trastes
                    :let [octave? (zero? (mod (:traste t) 12))
                          bar-w   (Math/round (* (/ (:espacamento-mm t) max-esp) 100.0))
                          nota    (nth notas (mod (:traste t) 12))]]
                (str "<tr class=\"" (if octave? "octave" "") "\">"
                       "<td><span class=\"traste-num\">" (:traste t) "</span></td>"
                       "<td>" (format "%.2f" (:dist-pestana-mm t)) "</td>"
                       "<td>" (format "%.2f" (:dist-ponte-mm t)) "</td>"
                       "<td>" (format "%.2f" (:espacamento-mm t)) "</td>"
                       "<td>" nota "</td>"
                       "<td class=\"bar-cell\"><div class=\"bar-bg\"><div class=\"bar-fill\" style=\"width:" bar-w "%\"></div></div></td>"
                     "</tr>")))
          "</tbody>"
        "</table>"
      "</div>")))

(defn- render [& {:keys [escala trastes resultado erro]
                 :or   {escala "650" trastes "22" resultado "" erro ""}}]
  (-> (slurp template-path)
      (str/replace "{{escala}}"    escala)
      (str/replace "{{trastes}}"   trastes)
      (str/replace "{{resultado}}" resultado)
      (str/replace "{{erro}}"      erro)))

(defn- html-response [body]
  (-> (response body) (content-type "text/html;charset=utf-8")))

(defroutes app-routes
  (GET "/" []
    (html-response (render :resultado empty-state)))

  (GET "/trastes" [escala trastes]
    (let [L (try (Double/parseDouble (str escala)) (catch Exception _ nil))
          n (or (try (Integer/parseInt (str trastes)) (catch Exception _ nil)) 22)]
      (if (and L (>= L 400) (<= L 1000))
        (html-response (render :escala    escala
                               :trastes   (str n)
                               :resultado (gerar-resultado-html L (calc/calcular L n))))
        (html-response (render :escala    (or escala "650")
                               :trastes   (or trastes "22")
                               :erro      "Informe 'escala' entre 400 e 1000 mm"
                               :resultado empty-state)))))

  (GET "/frontend/css/style.css" []
    (-> (ring.util.response/file-response (str frontend-path "/css/style.css"))
        (content-type "text/css;charset=utf-8")))
          
(GET "/api/trastes" [escala trastes]
    (let [L (try (Double/parseDouble (str escala)) (catch Exception _ nil))
          n (or (try (Integer/parseInt (str trastes)) (catch Exception _ nil)) 22)]
      (if L
        (let [dados (calc/calcular L n)]
          (-> (response (json/write-str dados))
              (content-type "application/json;charset=utf-8")))
        (-> (response (json/write-str {:erro "Escala inválida"})) 
            (content-type "application/json;charset=utf-8")
            (ring.util.response/status 400)))))

  (route/not-found "Página não encontrada"))