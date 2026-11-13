(ns phosphene.seme.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [phosphene.seme.core :as seme]
            [phosphene.seme.rheme :as rheme]
            [phosphene.seme.pheme :as pheme]
            [phosphene.seme.delome :as delome]
            [phosphene.seme.spec :as seme-spec]))

(deftest rheme-feature-algebra-test
  (testing "Unsaturated seme predicates and satisfaction"
    (let [s-telic (rheme/seme :seme.feat/telic (fn [v] (true? v)))
          s-animate (rheme/seme :seme.ont/animate (fn [v] (#{:human :animal} v)))]
      (is (true? (rheme/test-seme s-telic true)))
      (is (false? (rheme/test-seme s-telic false)))
      (is (true? (rheme/test-seme s-animate :human)))
      (is (false? (rheme/test-seme s-animate :stone)))))

  (testing "Componential sememe bundling and plerematic difference"
    (let [s1 (rheme/seme :seme.feat/telic (constantly true))
          s2 (rheme/seme :seme.ont/animate (constantly true))
          s3 (rheme/seme :seme.phon/nasal (constantly true))
          sememe-verb (rheme/sememe [s1 s2])
          sememe-noun (rheme/sememe [s2 s3])
          diff (rheme/plerematic-difference sememe-verb sememe-noun)]
      (is (= #{:seme.feat/telic} (:only-a diff)))
      (is (= #{:seme.phon/nasal} (:only-b diff)))
      (is (= #{:seme.ont/animate} (:shared diff))))))

(deftest pheme-witness-and-projection-test
  (s/def :test.ling/id string?)
  (s/def :test.phon/ipa string?)
  (s/def :test.syn/pos #{:noun :verb})

  (s/def :test.vertex/phonology
    (s/keys :req [:test.ling/id :test.phon/ipa]))

  (s/def :test.vertex/morphosyntax
    (s/keys :req [:test.ling/id :test.syn/pos]))

  (testing "Proof-carrying conformed witness on open referent"
    (let [referent {:test.ling/id "w1"
                    :test.phon/ipa "/kæt/"
                    :test.syn/pos :noun
                    :extra/open-world-key 42}
          witness (pheme/conformed-witness :test.vertex/phonology referent)]
      (is (true? (:witness/valid? witness)))
      (is (= referent (:witness/referent witness)))
      (is (= :test.vertex/phonology (:witness/spec witness)))
      (is (some? (:witness/anatomy witness)))))

  (testing "Multi-vertex co-projection without closed-world collision"
    (let [referent {:test.ling/id "w1"
                    :test.phon/ipa "/kæt/"
                    :test.syn/pos :noun}
          projections (pheme/project-vertices
                        {:phon :test.vertex/phonology
                         :syntax :test.vertex/morphosyntax}
                        referent)]
      (is (true? (get-in projections [:phon :witness/valid?])))
      (is (true? (get-in projections [:syntax :witness/valid?])))))

  (testing "Diagnostic anomaly extraction on invalid witness"
    (let [broken-referent {:test.phon/ipa "/kæt/"}
          witness (pheme/conformed-witness :test.vertex/phonology broken-referent)
          anomaly (pheme/explain-anomaly witness)]
      (is (false? (:witness/valid? witness)))
      (is (pos? (:anomaly/problem-count anomaly)))
      (is (= :test.vertex/phonology (:anomaly/spec anomaly))))))

(deftest pheme-vertex-lattice-test
  (testing "Vertex construction and record invariants"
    (let [v (pheme/vertex :v/phonology [:ling/id :phon/ipa] [:phon/pitch])]
      (is (true? (pheme/vertex? v)))
      (is (= #{:ling/id :phon/ipa} (:vertex/req v)))
      (is (= #{:phon/pitch} (:vertex/opt v)))))

  (testing "Keyset subsumption order relation"
    (let [v-base (pheme/vertex :v/base [:ling/id])
          v-phon (pheme/vertex :v/phon [:ling/id :phon/ipa])
          v-full (pheme/vertex :v/full [:ling/id :phon/ipa :syn/pos])]
      ;; Reflexivity: V >= V
      (is (true? (pheme/subsumes? v-phon v-phon)))
      ;; Strict ordering: v-full subsumes v-phon, which subsumes v-base
      (is (true? (pheme/subsumes? v-full v-phon)))
      (is (true? (pheme/subsumes? v-phon v-base)))
      (is (true? (pheme/subsumes? v-full v-base)))
      ;; Transitivity check
      (is (false? (pheme/subsumes? v-base v-phon)))
      (is (false? (pheme/subsumes? v-phon v-full)))))

  (testing "Vertex Meet (/\\) — greatest lower bound"
    (let [v-phon (pheme/vertex :v/phon [:ling/id :phon/ipa :shared/tag])
          v-syn  (pheme/vertex :v/syn  [:ling/id :syn/pos :shared/tag])
          v-meet (pheme/vertex-meet v-phon v-syn)]
      ;; Required keys is exactly intersection
      (is (= #{:ling/id :shared/tag} (:vertex/req v-meet)))
      ;; Both parent vertices subsume their meet
      (is (true? (pheme/subsumes? v-phon v-meet)))
      (is (true? (pheme/subsumes? v-syn v-meet)))))

  (testing "Vertex Join (\\/) — least upper bound"
    (let [v-phon (pheme/vertex :v/phon [:ling/id :phon/ipa])
          v-syn  (pheme/vertex :v/syn  [:ling/id :syn/pos])
          v-join (pheme/vertex-join v-phon v-syn)]
      ;; Required keys is exactly union
      (is (= #{:ling/id :phon/ipa :syn/pos} (:vertex/req v-join)))
      ;; The join subsumes both parent vertices
      (is (true? (pheme/subsumes? v-join v-phon)))
      (is (true? (pheme/subsumes? v-join v-syn))))))

(deftest delome-habit-inference-test
  (testing "Inductive distillation of Mode-D candidate spec from Mode-S empirical corpus"
    (let [corpus [{:id "1" :count 10 :label "alpha"}
                  {:id "2" :count 20 :label "beta"}
                  {:id "3" :count 30 :extra true}]
          candidate (delome/infer-candidate-spec corpus)]
      ;; :id and :count are present in all 3 specimens
      (is (= #{:id :count} (set (:inferred/req candidate))))
      ;; :label and :extra are present in only a subset
      (is (= #{:label :extra} (set (:inferred/opt candidate))))
      (is (= 'clojure.core/string? (get-in candidate [:inferred/preds :id])))
      (is (= 'clojure.core/integer? (get-in candidate [:inferred/preds :count])))))

  (testing "Differential falsification boundary specimen identification and automated synthesis"
    (s/def :test.bound/v1 (s/keys :req-un [::a]))
    (s/def :test.bound/v2 (s/keys :req-un [::a ::b]))
    (let [boundary-referent {:a 1}]
      (is (true? (delome/boundary-specimen? :test.bound/v1 :test.bound/v2 boundary-referent)))
      (is (false? (delome/boundary-specimen? :test.bound/v2 :test.bound/v1 boundary-referent))))

    (let [va (pheme/vertex :va [:test/x])
          vb (pheme/vertex :vb [:test/x :test/y])
          res (delome/synthesize-boundary-specimen va vb)]
      (is (true? (:found? res)))
      (is (contains? (:specimen res) :test/x))
      (is (not (contains? (:specimen res) :test/y)))
      (is (true? (delome/boundary-specimen? va vb (:specimen res)))))))

(deftest generative-spec-check-test
  (testing "Generative property checks across seme primitives and vertex lattice"
    (let [res (stest/check [`rheme/seme
                            `rheme/sememe
                            `rheme/plerematic-difference
                            `pheme/conformed-witness
                            `pheme/subsumes?
                            `pheme/vertex-meet
                            `pheme/vertex-join
                            `delome/infer-candidate-spec
                            `delome/synthesize-boundary-specimen]
                           {:clojure.spec.test.check/opts {:num-tests 100}})]
      (doseq [r res]
        (let [sym (:sym r)
              check-ret (:clojure.spec.test.check/ret r)
              err (:error check-ret)]
          (is (nil? err) (str "Generative check failed for " sym ": " (pr-str check-ret)))
          (is (true? (:result check-ret)) (str "Check was not true for " sym)))))))
