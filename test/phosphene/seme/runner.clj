(ns phosphene.seme.runner
  (:require [clojure.test :refer [run-tests]]
            [phosphene.seme.core-test]
            [phosphene.seme.integration-test]))

(defn -main [& _args]
  (println "Running seme test suite (unit + clojure.spec/test.check + cross-trifecta integration)...")
  (let [summary (run-tests 'phosphene.seme.core-test
                           'phosphene.seme.integration-test)]
    (if (and (zero? (:fail summary)) (zero? (:error summary)))
      (do
        (println "All tests, generative checks, and integration pipelines passed cleanly!")
        (System/exit 0))
      (do
        (println "Test failures encountered:" summary)
        (System/exit 1)))))
