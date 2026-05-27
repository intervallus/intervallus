(ns intervallus.calculos)

(defn- arredondar [v]
  (Double/parseDouble (String/format java.util.Locale/US "%.2f" (into-array Object [(double v)]))))

(defn calcular [escala num-trastes]
  (loop [n 1, d-ant 0.0, resultado []]
    (if (> n num-trastes)
      resultado
      (let [d-pestana   (* escala (- 1.0 (/ 1.0 (Math/pow 2.0 (/ n 12.0)))))
            espacamento (if (= n 1) d-pestana (- d-pestana d-ant))]
        (recur (inc n)
               d-pestana
               (conj resultado {:traste          n
                                :dist-pestana-mm (arredondar d-pestana)
                                :dist-ponte-mm   (arredondar (- escala d-pestana))
                                :espacamento-mm  (arredondar espacamento)}))))))  