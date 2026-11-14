(ns phosphene.seme.core-test
  (:require [clojure.test :refer :all]
            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [phosphene.seme.rheme :as rheme]
            [phosphene.seme.pheme :as pheme]
            [phosphene.seme.delome :as delome]
            [phosphene.seme.chain :as chain]
            [phosphene.seme.complementarity :as comp]
            [phosphene.seme.markdown :as md]
            [phosphene.seme.bridge :as bridge]
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

(deftest pheme-derivation-chain-test
  (s/def :test.pipe/str-int (s/and string? #(re-matches #"\d+" %)))
  (s/def :test.pipe/parsed-int (s/conformer #(Integer/parseInt %)))
  (s/def :test.pipe/positive pos?)

  (testing "Multi-stage sequential derivation chain with re-hydration"
    (let [pipeline [:test.pipe/str-int :test.pipe/parsed-int :test.pipe/positive]
          witness (pheme/derivation-chain-witness pipeline "42")]
      (is (true? (:witness/valid? witness)))
      (is (= "42" (:witness/initial witness)))
      (is (= 42 (:witness/final witness)))
      (is (= 3 (count (:witness/steps witness))))
      (is (nil? (:witness/failed-step witness)))
      ;; Check intermediate conformed values
      (is (= "42" (get-in (:witness/steps witness) [0 :step/output])))
      (is (= 42 (get-in (:witness/steps witness) [1 :step/output])))
      (is (= 42 (get-in (:witness/steps witness) [2 :step/output])))))

  (testing "Derivation chain failure localization and anomaly isolation"
    (let [pipeline [:test.pipe/str-int :test.pipe/parsed-int :test.pipe/positive]
          witness (pheme/derivation-chain-witness pipeline "-10")]
      ;; Step 0 passes ("-10" is not digits, so fails at step 0)
      (is (false? (:witness/valid? witness)))
      (is (= 0 (:witness/failed-step witness)))
      (is (= 1 (count (:witness/steps witness))))
      (is (some? (get-in (:witness/steps witness) [0 :step/anomaly]))))))

(deftest delome-habit-and-synthesis-test
  (testing "Contextual overlays and empirical spec distillation"
    (let [base {:attr/score number?}
          overlay (delome/contextual-overlay base {:attr/score (fn [x] (and (number? x) (pos? x)))})]
      (is (true? (delome/evaluate-overlay overlay :attr/score 10)))
      (is (false? (delome/evaluate-overlay overlay :attr/score -5))))

    (let [corpus [{:id "1" :count 10 :tag :a}
                  {:id "2" :count 20}
                  {:id "3" :count 30 :tag :b}]
          candidate (delome/infer-candidate-spec corpus)]
      (is (= #{:id :count} (set (:inferred/req candidate))))
      (is (= #{:tag} (set (:inferred/opt candidate))))
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
      (is (true? (delome/boundary-specimen? va vb (:specimen res))))))

  (testing "Unqualified Mode-S ingress bridge and boundary re-hydration (CLJ-002-U, TELL-1002)"
    (s/def :seme.phon/ipa string?)
    (s/def :seme.syn/pos #{:noun :verb})

    ;; Case 1: Re-hydrate using a namespace prefix
    (let [raw-json-map {"ipa" "/kæt/" "pos" :noun "other" 123}
          res (delome/rehydrate-unqualified raw-json-map :seme.phon {:preserve-unmapped? false})]
      (is (true? (:rehydrated/valid? res)))
      (is (= "/kæt/" (get-in res [:rehydrated/referent :seme.phon/ipa])))
      (is (nil? (:rehydrated/anomalies res))))

    ;; Case 2: Re-hydrate using explicit coordinate alias mapping dictionary
    (let [raw-dict {"ipa" "/dɒɡ/" "pos" :noun}
          mapping {"ipa" :seme.phon/ipa "pos" :seme.syn/pos}
          res (delome/rehydrate-unqualified raw-dict mapping)]
      (is (true? (:rehydrated/valid? res)))
      (is (= "/dɒɡ/" (get-in res [:rehydrated/referent :seme.phon/ipa])))
      (is (= :noun (get-in res [:rehydrated/referent :seme.syn/pos]))))

    ;; Case 3: Re-hydration validation failure detection (grounding bleed guard)
    (let [corrupt-dict {"ipa" 12345 "pos" :noun}
          mapping {"ipa" :seme.phon/ipa "pos" :seme.syn/pos}
          res (delome/rehydrate-unqualified corrupt-dict mapping)]
      (is (false? (:rehydrated/valid? res)))
      (is (some? (get-in res [:rehydrated/anomalies :seme.phon/ipa]))))))

(deftest chain-grammar-test
  (testing "Slot construction and validation"
    (let [s1 (chain/slot :subj string?)
          s2 (chain/slot :verb keyword? :opt)
          s3 (chain/slot :mods symbol? :star)]
      (is (true? (chain/slot? s1)))
      (is (= :subj (:slot/name s1)))
      (is (= :opt (:slot/cardinality s2)))
      (is (= :star (:slot/cardinality s3)))))

  (testing "Sequential predicative chain grammar parsing (CLJ-007, CLJ-007-C/A/R)"
    (let [g (chain/chain-grammar :grammar/sov
              [(chain/slot :subj (fn [x] (= (:pos x) :noun)))
               (chain/slot :obj  (fn [x] (= (:pos x) :noun)))
               (chain/slot :verb (fn [x] (= (:pos x) :verb)))])
          tok-seq [{:word "cat" :pos :noun}
                   {:word "fish" :pos :noun}
                   {:word "eats" :pos :verb}]
          witness (chain/conformed-chain-witness g tok-seq)]
      (is (true? (:chain/valid? witness)))
      (is (= {:word "cat" :pos :noun} (get-in witness [:chain/parsed :subj])))
      (is (= {:word "fish" :pos :noun} (get-in witness [:chain/parsed :obj])))
      (is (= {:word "eats" :pos :verb} (get-in witness [:chain/parsed :verb])))
      (is (empty? (:chain/unconsumed witness))))

    (let [g (chain/chain-grammar :grammar/svo
              [(chain/slot :subj (fn [x] (= (:pos x) :noun)))
               (chain/slot :verb (fn [x] (= (:pos x) :verb)))
               (chain/slot :obj  (fn [x] (= (:pos x) :noun)) :opt)])
          valid-seq [{:word "dog" :pos :noun} {:word "barks" :pos :verb}]
          invalid-seq [{:word "barks" :pos :verb} {:word "dog" :pos :noun}]]
      (is (true? (chain/validate-chain g valid-seq)))
      (is (false? (chain/validate-chain g invalid-seq)))
      (let [fail-witness (chain/conformed-chain-witness g invalid-seq)]
        (is (false? (:chain/valid? fail-witness)))
        (is (= :subj (:chain/failed-slot fail-witness)))))))

(deftest epistemic-cycle-and-contingency-test
  (testing "Predication precedes prediction (SEME-005, SEME-014)"
    (s/def :seme.phenom/luminosity (s/and number? #(<= 0.0 % 1.0)))
    (s/def :seme.phenom/thermal-flux (s/and number? pos?))
    (s/def :seme.phenom/flame-event (s/keys :req [:seme.phenom/luminosity]))
    (s/def :seme.delome/heat-expected (s/keys :req [:seme.phenom/thermal-flux]))

    ;; Stage 1: Phemic predication fails on raw encounter
    (let [invalid-encounter {:seme.phenom/luminosity -5.0}
          res (delome/epistemic-cycle :seme.phenom/flame-event :seme.delome/heat-expected invalid-encounter)]
      (is (= :phemic-predication-failed (:stage res)))
      (is (some? (:anomaly res))))

    ;; Stage 2: Phemic predication succeeds, but contingent prediction fails (Diagnostic Surprise, SEME-010)
    (let [lum-only-encounter {:seme.phenom/luminosity 0.9}
          res (delome/epistemic-cycle :seme.phenom/flame-event :seme.delome/heat-expected lum-only-encounter)]
      (is (= :contingent-prediction-falsified (:stage res)))
      (is (false? (:logically-necessary? res)))
      (is (= :diagnostic-surprise (get-in res [:surprise :surprise/kind])))
      (is (some? (get-in res [:surprise :surprise/anomaly]))))

    ;; Stage 3: Full confirmation through customary habit loop
    (let [full-flame {:seme.phenom/luminosity 0.95 :seme.phenom/thermal-flux 410.0}
          res (delome/epistemic-cycle :seme.phenom/flame-event :seme.delome/heat-expected full-flame)]
      (is (= :delomic-habit-confirmed (:stage res)))
      (is (= :habit-borne-out (:status res)))
      (is (false? (:logically-necessary? res)))
      (is (= full-flame (:conformed res)))))

  (testing "Delomic pragmatic would-be contracts (SEME-012)"
    (s/def :seme.agent/stimulus (s/keys :req-un [::intensity]))
    (s/def :seme.agent/response (s/keys :req-un [::action]))
    (s/def ::intensity number?)
    (s/def ::action #{:flee :approach})

    (let [reflex-fn (fn [s] {:action (if (> (:intensity s) 50) :flee :approach)})
          c1 (delome/pragmatic-conditional :seme.agent/stimulus reflex-fn :seme.agent/response {:intensity 80})
          c2 (delome/pragmatic-conditional :seme.agent/stimulus reflex-fn :seme.agent/response {:intensity 20})]
      (is (true? (:would-be/holds? c1)))
      (is (= :flee (get-in c1 [:would-be/consequence :action])))
      (is (true? (:would-be/holds? c2)))
      (is (= :approach (get-in c2 [:would-be/consequence :action]))))))

  (testing "Poset lattice retraction and structural provocation (T-SEM-009)"
    (s/def :seme.phenom/luminosity (s/and number? #(<= 0.0 % 1.0)))
    (s/def :seme.phenom/thermal-flux (s/and number? pos?))
    (let [v-flame (pheme/vertex :v/flame-event [:seme.phenom/luminosity :seme.phenom/thermal-flux])
          ;; Specimen satisfying luminosity but violating thermal-flux (cryogenic luminous event)
          anomalous-specimen {:seme.phenom/luminosity 0.85 :seme.phenom/thermal-flux -10.0}
          retraction (pheme/retract-vertex v-flame anomalous-specimen)
          provocation (pheme/lattice-provocation v-flame anomalous-specimen)]

      ;; Retraction properties
      (is (false? (:retraction/fully-satisfied? retraction)))
      (is (= #{:seme.phenom/luminosity} (:retraction/admitted-keys retraction)))
      (is (= #{:seme.phenom/thermal-flux} (:retraction/falsified-keys retraction)))
      (is (true? (pheme/subsumes? v-flame (:retraction/retracted-vertex retraction))))

      ;; Provocation proof object
      (is (some? provocation))
      (is (= :structural-requirement-falsification (:provocation/type provocation)))
      (is (= :v/flame-event (:provocation/claimed-vertex provocation)))
      (is (= #{:seme.phenom/thermal-flux} (:provocation/provoked-axioms provocation)))
      (is (= #{:seme.phenom/luminosity} (:provocation/preserved-axioms provocation)))

      ;; Fully satisfied case yields nil provocation
      (let [valid-specimen {:seme.phenom/luminosity 0.9 :seme.phenom/thermal-flux 150.0}
            valid-ret (pheme/retract-vertex v-flame valid-specimen)
            nil-prov (pheme/lattice-provocation v-flame valid-specimen)]
        (is (true? (:retraction/fully-satisfied? valid-ret)))
        (is (nil? nil-prov)))))

  (testing "Tier disentanglement and retrograde ancestral derivation tracing (T-SEM-010)"
    ;; 1. Tier Disentanglement
    (s/def :seme.bio/cell-count pos-int?)
    ;; Ingress Shock (unnamespaced coordinate)
    (let [t1 (delome/disentangle-provocation-tier :cell-count 5)]
      (is (= :tier/perimeter-ingress-shock (:tier/classification t1)))
      (is (= :ingress-bridge (:tier/target-subsystem t1))))

    ;; Constitutive Invariant Failure (violates global base spec)
    (let [t2 (delome/disentangle-provocation-tier :seme.bio/cell-count -10)]
      (is (= :tier/constitutive-invariant (:tier/classification t2)))
      (is (= :sovereign-registry (:tier/target-subsystem t2))))

    ;; Selectional Restriction Failure (satisfies base spec, violates vertex overlay)
    (let [overlay {:seme.bio/cell-count (fn [v] (> v 100))}
          t3 (delome/disentangle-provocation-tier :seme.bio/cell-count 50 overlay)]
      (is (= :tier/selectional-restriction (:tier/classification t3)))
      (is (= :vertex-overlay (:tier/target-subsystem t3))))

    ;; Sound
    (let [overlay {:seme.bio/cell-count (fn [v] (> v 100))}
          t4 (delome/disentangle-provocation-tier :seme.bio/cell-count 150 overlay)]
      (is (= :tier/sound (:tier/classification t4)))
      (is (= :none (:tier/target-subsystem t4))))

    ;; 2. Retrograde Derivation Ancestor Trace
    ;; Pipeline: stage 0 conforms input by doubling count, stage 1 requires count <= 15
    (let [p0 (fn [m] (update m :count * 2))
          p1 (s/spec (fn [m] (<= (:count m) 15)))
          witness (pheme/derivation-chain-witness [p0 p1] {:count 10})
          ancestor (pheme/retrograde-ancestor-trace witness :count)]
      (is (false? (:witness/valid? witness)))
      (is (= 1 (:witness/failed-step witness)))
      (is (true? (:ancestor/found? ancestor)))
      (is (= 0 (:ancestor/step-index ancestor)))
      (is (= 20 (:ancestor/conditioned-value ancestor)))
      (is (= 1 (:ancestor/failing-step-index ancestor)))))

(deftest non-commutative-complementarity-test
  (testing "Wittgensteinian hinge namespace extraction and sovereignty"
    (is (= :newton.mechanics (comp/extract-hinge-namespace :newton.mechanics/mass)))
    (is (= :einstein.relativity (comp/extract-hinge-namespace :einstein.relativity/mass)))
    (is (true? (comp/sovereign-hinges-distinct?
                 (pheme/vertex :newton.mechanics/v [:newton.mechanics/mass])
                 (pheme/vertex :einstein.relativity/v [:einstein.relativity/mass])))))

  (testing "Operational non-commutative commutator"
    ;; Commutative operations: +1 and +2
    (let [c-comm (comp/commutator #(update % :x + 1) #(update % :x + 2) {:x 0})]
      (is (true? (:commutator/zero? c-comm)))
      (is (= 3 (get-in c-comm [:commutator/ab-result :x])))
      (is (empty? (:commutator/residual c-comm))))

    ;; Non-commutative operations: (* 2) and (+ 3)
    ;; (T_B (T_A x)) = (2 * x) + 3
    ;; (T_A (T_B x)) = 2 * (x + 3) = 2x + 6
    (let [c-non-comm (comp/commutator #(update % :x * 2) #(update % :x + 3) {:x 10})]
      (is (false? (:commutator/zero? c-non-comm)))
      (is (= 23 (get-in c-non-comm [:commutator/ab-result :x])))
      (is (= 26 (get-in c-non-comm [:commutator/ba-result :x])))
      (is (= {:ab 23 :ba 26} (get-in c-non-comm [:commutator/residual :x])))))

  (testing "Complementary dyad, anti-collapse gate, and incommensurability dossier"
    ;; Define Newtonian vs Relativistic vertices
    (s/def :newton.phys/velocity-independent-mass number?)
    (s/def :newton.phys/spatial-coordinate vector?)
    (s/def :einstein.rel/invariant-rest-mass number?)
    (s/def :einstein.rel/spacetime-interval number?)

    (let [v-newton (pheme/vertex :newton.phys/vertex
                                 [:newton.phys/velocity-independent-mass
                                  :newton.phys/spatial-coordinate])
          v-einstein (pheme/vertex :einstein.rel/vertex
                                   [:einstein.rel/invariant-rest-mass
                                    :einstein.rel/spacetime-interval])
          dyad (comp/complementary-dyad :dyad/mechanics v-newton v-einstein
                                        {:domain :kinematics :scale-separation :meso-vs-relativistic})
          dossier (comp/incommensurability-dossier dyad)]

      (is (true? (comp/complementary-dyad? dyad)))
      (is (true? (:dossier/incommensurable? dossier)))
      (is (= #{:newton.phys/velocity-independent-mass :newton.phys/spatial-coordinate}
             (:dossier/only-a-keys dossier)))
      (is (= #{:einstein.rel/invariant-rest-mass :einstein.rel/spacetime-interval}
             (:dossier/only-b-keys dossier)))
      (is (= #{:newton.phys :einstein.rel} (:dossier/hinge-namespaces dossier)))

      ;; Anti-collapse invariant breach check
      (is (thrown-with-msg?
            clojure.lang.ExceptionInfo
            #"Anti-Collapse Invariant Breach"
            (comp/assert-non-collapsible! dyad)))))

  (testing "Bohrian Dual-Aspect Proof Witness & Scale Regimes"
    (s/def :scale.meso/speed (s/and number? #(<= % 1000.0)))
    (s/def :scale.meso/mass-invariant? true?)
    (s/def :scale.rel/gamma (s/and number? #(>= % 1.0)))

    (let [v-meso (pheme/vertex :v/meso-scale [:scale.meso/speed :scale.meso/mass-invariant?])
          v-rel (pheme/vertex :v/relativistic [:scale.rel/gamma])
          dyad (comp/complementary-dyad :dyad/scale-transition v-meso v-rel)

          ;; Specimen A: Low-velocity baseball (40 m/s)
          specimen-baseball {:scale.meso/speed 40.0
                             :scale.meso/mass-invariant? true
                             :scale.rel/gamma 1.0}
          wit-baseball (comp/dual-aspect-witness dyad specimen-baseball)

          ;; Specimen B: High-velocity cosmic muon (gamma = 10, mass invariance fails)
          specimen-muon {:scale.meso/speed 299000000.0
                         :scale.meso/mass-invariant? false
                         :scale.rel/gamma 10.0}
          wit-muon (comp/dual-aspect-witness dyad specimen-muon)]

      ;; Baseball satisfies both (correspondence limit holds)
      (is (true? (:witness/both-valid? wit-baseball)))
      (is (= :both-hold (:witness/regime wit-baseball)))
      (is (= specimen-baseball (:witness/referent wit-baseball)))

      ;; Muon exceeds meso-scale limit: meso fails, relativistic holds
      (is (false? (:witness/both-valid? wit-muon)))
      (is (= :aspect-b-only (:witness/regime wit-muon)))
      (is (false? (get-in wit-muon [:witness/aspect-a :witness/valid?])))
      (is (true? (get-in wit-muon [:witness/aspect-b :witness/valid?])))
      (is (= specimen-muon (:witness/referent wit-muon))))))

(deftest markdown-syntagmatic-chain-and-python-bridge-test
  (testing "Tokenizing text into distinctive symbol keywords"
    (let [tokens (md/tokenize-text "Mitchell proposed chemiosmotic coupling across intact lipid membranes!")]
      (is (= [:mitchell :proposed :chemiosmotic :coupling :across :intact :lipid :membranes] tokens))))

  (testing "Parsing structured Markdown AST blocks with coordinate tags"
    (let [sample-doc (str "---\nauthor: Mitchell\nyear: 1961\n---\n\n"
                          "# Bioenergetics\n\n"
                          "Vectorial metabolism is fundamental.\n\n"
                          "> Coupling requires intact membranes.\n\n"
                          "* Electron transport\n* Proton gradient\n")
          blocks (md/parse-markdown-blocks "doc-mitchell" sample-doc)]
      (is (= 6 (count blocks)))
      (is (= :frontmatter (:block-type (nth blocks 0))))
      (is (= "doc-mitchell:frontmatter" (:coordinate (nth blocks 0))))
      (is (= :heading (:block-type (nth blocks 1))))
      (is (= 1 (:level (nth blocks 1))))
      (is (= :paragraph (:block-type (nth blocks 2))))
      (is (= :blockquote (:block-type (nth blocks 3))))
      (is (= :list-item (:block-type (nth blocks 4))))
      (is (= :list-item (:block-type (nth blocks 5))))))

  (testing "Syntagmatic Chain Grammar parsing over Markdown AST stream"
    (let [grammar (chain/chain-grammar
                    :grammar/scientific-note
                    [(chain/slot :header (fn [b] (= :heading (:block-type b))))
                     (chain/slot :claim (fn [b] (= :paragraph (:block-type b))))
                     (chain/slot :evidence (fn [b] (= :blockquote (:block-type b))))])
          doc-text "# Hypothesis\n\nChemiosmosis explains ATP synthesis.\n\n> Measured proton motive force was 200mV.\n"
          blocks (md/parse-markdown-blocks "doc-proof" doc-text)
          witness (chain/conformed-chain-witness grammar blocks)]
      (is (true? (:chain/valid? witness)))
      (is (= :grammar/scientific-note (:chain/grammar witness)))
      (is (= "Hypothesis" (get-in witness [:chain/parsed :header :raw-text])))
      (is (= "Chemiosmosis explains ATP synthesis." (get-in witness [:chain/parsed :claim :raw-text])))
      (is (= "Measured proton motive force was 200mV." (get-in witness [:chain/parsed :evidence :raw-text])))))

  (testing "Manifest Ingress Rehydration (Mode-S -> Mode-D Bridge)"
    (s/def :test.entry/id string?)
    (s/def :test.entry/unit-of-reason string?)
    (s/def :test.entry/page pos-int?)
    (s/def :test.manifest/entry (s/keys :req [:test.entry/id :test.entry/unit-of-reason]
                                        :opt [:test.entry/page]))

    (let [raw-manifest-entries [{:id "CR-001" :unit_of_reason "Information alone fails" :page 24}
                                {:id "CR-002" :unit_of_reason "Deep eukaryotic homology" :page 25}]
          alias-map {:id :test.entry/id
                     :unit_of_reason :test.entry/unit-of-reason
                     :page :test.entry/page}
          rehydrated (bridge/ingest-manifest-entries raw-manifest-entries alias-map)]
      (is (= 2 (count rehydrated)))
      (is (true? (get-in rehydrated [0 :rehydrated/valid?])))
      (is (= "CR-001" (get-in rehydrated [0 :rehydrated/referent :test.entry/id])))
      (is (= "Information alone fails" (get-in rehydrated [0 :rehydrated/referent :test.entry/unit-of-reason])))
      (is (= 24 (get-in rehydrated [0 :rehydrated/referent :test.entry/page])))))

  (testing "External Python Verifier Script Invocation"
    (let [workspace-dir "/home/node/.openclaw/workspace"
          py-script "work/topic-98577-public-evolutionary-science/scripts/verify_close_read.py"
          res (bridge/run-python-verifier {:dir workspace-dir} py-script)]
      (if (:verifier/success? res)
        (do
          (is (true? (:verifier/success? res)))
          (is (= 0 (:verifier/exit-code res)))
          (is (clojure.string/includes? (:verifier/stdout res) "AUDIT PASSED")))
        ;; In hermetic CI environments where the host workspace path does not exist,
        ;; verify that the bridge gracefully caught the IO exception rather than crashing
        (do
          (is (false? (:verifier/success? res)))
          (is (= -1 (:verifier/exit-code res)))
          (is (= :io-exception (:verifier/error-type res))))))))

(deftest advanced-predicative-regex-grammars-test
  (testing "alt-slot deterministic first-match priority and tagged branch AST"
    (let [g (chain/chain-grammar
              :grammar/evidence-arc
              [(chain/slot :claim string?)
               (chain/alt-slot :evidence
                               [[:exact-citation (s/and string? #(clojure.string/starts-with? % ">"))]
                                [:code-snippet   (s/and string? #(clojure.string/starts-with? % "`"))]
                                [:general-note   string?]]
                               :plus)])
          tokens-citation ["Mitchell's principle" "> Intact membrane required"]
          tokens-code     ["Vectorial coupling" "`pmf = -59*dpH + dPsi`"]
          tokens-fallback ["General claim" "Plain observational note"]

          wit-citation (chain/conformed-chain-witness g tokens-citation)
          wit-code     (chain/conformed-chain-witness g tokens-code)
          wit-fallback (chain/conformed-chain-witness g tokens-fallback)]

      (is (true? (:chain/valid? wit-citation)))
      (is (= [[:exact-citation "> Intact membrane required"]]
             (get-in wit-citation [:chain/parsed :evidence])))

      (is (true? (:chain/valid? wit-code)))
      (is (= [[:code-snippet "`pmf = -59*dpH + dPsi`"]]
             (get-in wit-code [:chain/parsed :evidence])))

      (is (true? (:chain/valid? wit-fallback)))
      (is (= [[:general-note "Plain observational note"]]
             (get-in wit-fallback [:chain/parsed :evidence])))))

  (testing "nested-slot scoped hierarchical sub-grammar without token flattening"
    (let [ordeal-grammar (chain/chain-grammar
                           :grammar/underworld-ordeal
                           [(chain/slot :trial keyword?)
                            (chain/slot :response keyword?)])
          hero-myth-grammar (chain/chain-grammar
                              :grammar/hero-journey
                              [(chain/slot :departure string?)
                               (chain/nested-slot :ordeal ordeal-grammar :one)
                               (chain/slot :return string?)])
          tokens ["Departure from Highland Milpa"
                  [:trial/dark-house :response/cigar-glow]
                  "Return with Corn Seeds"]
          witness (chain/conformed-chain-witness hero-myth-grammar tokens)]

      (is (true? (:chain/valid? witness)))
      (is (= "Departure from Highland Milpa" (get-in witness [:chain/parsed :departure])))
      (is (= "Return with Corn Seeds" (get-in witness [:chain/parsed :return])))
      (let [nested-wit (get-in witness [:chain/parsed :ordeal])]
        (is (true? (:chain/valid? nested-wit)))
        (is (= :grammar/underworld-ordeal (:chain/grammar nested-wit)))
        (is (= :trial/dark-house (get-in nested-wit [:chain/parsed :trial])))
        (is (= :response/cigar-glow (get-in nested-wit [:chain/parsed :response]))))))

  (testing "nested-slot with repetition (:plus)"
    (let [inner-step (chain/chain-grammar
                       :grammar/step
                       [(chain/slot :action keyword?)])
          pipeline-grammar (chain/chain-grammar
                             :grammar/pipeline
                             [(chain/slot :start string?)
                              (chain/nested-slot :steps inner-step :plus)
                              (chain/slot :finish string?)])
          tokens ["init" [:step/a] [:step/b] [:step/c] "done"]
          witness (chain/conformed-chain-witness pipeline-grammar tokens)]
      (is (true? (:chain/valid? witness)))
      (is (= 3 (count (get-in witness [:chain/parsed :steps]))))))

  (testing "grammar-commutator measuring structural divergence across competing models"
    (let [grammar-a (chain/chain-grammar
                      :grammar/imrad
                      [(chain/slot :intro string?)
                       (chain/slot :method string?)
                       (chain/slot :results string?)])
          grammar-b (chain/chain-grammar
                      :grammar/dialectical
                      [(chain/slot :thesis string?)
                       (chain/slot :antithesis string?)
                       (chain/slot :synthesis string?)])
          tokens ["Background & Problem"
                  "Experimental Methodology"
                  "Empirical Observations"]
          comm (chain/grammar-commutator grammar-a grammar-b tokens)]
      (is (true? (:commutator/both-valid? comm)))
      (is (= :both-match (:commutator/regime comm)))
      (is (= [:intro :method :results] (get-in comm [:commutator/slot-divergence :grammar-a-slots])))
      (is (= [:thesis :antithesis :synthesis] (get-in comm [:commutator/slot-divergence :grammar-b-slots])))
      (is (empty? (get-in comm [:commutator/slot-divergence :shared-slots]))))))

(deftest generative-spec-check-test
  (testing "Generative property checks across seme primitives, vertex lattice, and derivation chains"
    (let [res (stest/check [`rheme/seme
                            `rheme/sememe
                            `rheme/plerematic-difference
                            `pheme/conformed-witness
                            `pheme/subsumes?
                            `pheme/vertex-meet
                            `pheme/vertex-join
                            `pheme/derivation-chain-witness
                            `pheme/retract-vertex
                            `pheme/lattice-provocation
                            `pheme/retrograde-ancestor-trace
                            `delome/infer-candidate-spec
                            `delome/rehydrate-unqualified
                            `delome/disentangle-provocation-tier
                            `delome/contingent-prediction
                            `delome/epistemic-cycle
                            `delome/pragmatic-conditional
                            `delome/synthesize-boundary-specimen
                            `chain/chain-grammar
                            `chain/alt-slot
                            `chain/grammar-commutator
                            `chain/conformed-chain-witness
                            `comp/complementary-dyad
                            `comp/commutator
                            `comp/incommensurability-dossier
                            `comp/dual-aspect-witness
                            `md/tokenize-text
                            `md/parse-markdown-blocks
                            `bridge/ingest-manifest-entries]
                           {:clojure.spec.test.check/opts {:num-tests 100}})]
      (doseq [r res]
        (let [sym (:sym r)
              check-ret (:clojure.spec.test.check/ret r)
              err (:error check-ret)]
          (is (nil? err) (str "Generative check failed for " sym ": " (pr-str check-ret)))
          (is (true? (:result check-ret)) (str "Check was not true for " sym)))))))
