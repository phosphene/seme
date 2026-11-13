(ns phosphene.seme.spec
  "Formal specification and property invariants for phosphene.seme."
  (:require [clojure.set :as set]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [phosphene.seme.rheme :as rheme]
            [phosphene.seme.pheme :as pheme]
            [phosphene.seme.delome :as delome]
            [phosphene.seme.chain :as chain]))

;; --- Sovereign Attribute Specs & Generators ---

(s/def :seme.feat/id
  (s/with-gen keyword?
    #(gen/elements [:seme.feat/telic :seme.ont/animate :seme.phon/nasal :seme.feat/volitional :seme.feat/human])))

(s/def :seme.feat/pred
  (s/with-gen ifn?
    #(gen/return (constantly true))))

(s/def :seme.feat/metadata
  (s/with-gen map?
    #(gen/return {})))

(s/def ::seme
  (s/with-gen
    (s/and map?
           #(contains? % :seme/id)
           #(contains? % :seme/pred))
    #(gen/fmap (fn [k] (rheme/seme k (constantly true) {}))
               (s/gen :seme.feat/id))))

(s/def ::sememe
  (s/with-gen
    (s/and map?
           #(contains? % :sememe/semes)
           #(map? (:sememe/semes %)))
    #(gen/fmap (fn [semes] (rheme/sememe (vec semes)))
               (gen/vector (s/gen ::seme) 1 4))))

(s/def ::witness
  (s/and map?
         #(contains? % :witness/valid?)
         #(contains? % :witness/anatomy)
         #(contains? % :witness/referent)))

(s/def ::vertex
  (s/with-gen
    (s/and map?
           #(contains? % :vertex/id)
           #(contains? % :vertex/req)
           #(contains? % :vertex/opt))
    #(gen/fmap (fn [[id reqs opts]] (pheme/vertex id reqs opts {}))
               (gen/tuple (gen/elements [:v/phon :v/morph :v/sem :v/lex :v/prag])
                          (gen/vector (s/gen :seme.feat/id) 1 3)
                          (gen/vector (s/gen :seme.feat/id) 0 2)))))

(s/def ::slot
  (s/with-gen
    (s/and map?
           #(contains? % :slot/name)
           #(contains? % :slot/pred)
           #(contains? % :slot/cardinality))
    #(gen/fmap (fn [[k card]] (chain/slot k (constantly true) card))
               (gen/tuple (gen/elements [:s/subj :s/verb :s/obj :s/mod])
                          (gen/elements [:one :opt :star :plus])))))

(s/def ::chain-grammar
  (s/with-gen
    (s/and map?
           #(contains? % :grammar/id)
           #(contains? % :grammar/slots))
    #(gen/fmap (fn [slots] (chain/chain-grammar :grammar/test slots))
               (gen/vector (s/gen ::slot) 1 3))))

(s/def ::test-spec-coord
  (s/with-gen any?
    #(gen/elements [string? number? integer? boolean? keyword? map? vector?])))

(s/def ::action-fn
  (s/with-gen ifn?
    #(gen/elements [(fn [x] {:result x})
                    (fn [x] (str "action-" x))
                    (fn [x] {:action (if (number? x) :flee :approach)})])))

;; --- Function Specs: Rheme ---

(s/fdef rheme/seme
  :args (s/cat :id :seme.feat/id :pred :seme.feat/pred)
  :ret ::seme
  :fn (fn [{:keys [ret]}]
        (rheme/seme? ret)))

(s/fdef rheme/sememe
  :args (s/cat :semes (s/coll-of ::seme :kind vector?))
  :ret ::sememe
  :fn (fn [{:keys [ret]}]
        (rheme/sememe? ret)))

(s/fdef rheme/plerematic-difference
  :args (s/cat :sememe-a ::sememe :sememe-b ::sememe)
  :ret (s/keys :req-un [::only-a ::only-b ::shared])
  :fn (fn [{:keys [args ret]}]
        (let [{:keys [sememe-a sememe-b]} args
              a-ids (rheme/feature-ids sememe-a)
              b-ids (rheme/feature-ids sememe-b)]
          ;; Invariant: Union of only-a, only-b, and shared must equal union of a-ids and b-ids
          (= (set/union a-ids b-ids)
             (set/union (:only-a ret) (:only-b ret) (:shared ret))))))

;; --- Function Specs: Pheme & Vertex Lattice ---

(s/fdef pheme/conformed-witness
  :args (s/cat :spec-k (s/with-gen any? #(gen/return any?))
               :referent (s/with-gen any? #(gen/return {:sample/id "id-1"})))
  :ret ::witness
  :fn (fn [{:keys [args ret]}]
        (let [{:keys [referent]} args]
          ;; Invariant: Referent is preserved without mutation
          (= referent (:witness/referent ret)))))

(s/fdef pheme/subsumes?
  :args (s/cat :vertex-a ::vertex :vertex-b ::vertex)
  :ret boolean?
  :fn (fn [{:keys [args ret]}]
        (let [{:keys [vertex-a vertex-b]} args
              req-a (:vertex/req vertex-a)
              req-b (:vertex/req vertex-b)]
          ;; Invariant: subsumes? is true iff req-b is a subset of req-a
          (= ret (set/subset? req-b req-a)))))

(s/fdef pheme/vertex-meet
  :args (s/cat :vertex-a ::vertex :vertex-b ::vertex)
  :ret ::vertex
  :fn (fn [{:keys [args ret]}]
        (let [{:keys [vertex-a vertex-b]} args
              req-a (:vertex/req vertex-a)
              req-b (:vertex/req vertex-b)
              ret-req (:vertex/req ret)]
          ;; Invariant: Meet required keys is exactly the intersection
          ;; and both parents subsume the meet
          (and (= ret-req (set/intersection req-a req-b))
               (pheme/subsumes? vertex-a ret)
               (pheme/subsumes? vertex-b ret)))))

(s/fdef pheme/vertex-join
  :args (s/cat :vertex-a ::vertex :vertex-b ::vertex)
  :ret ::vertex
  :fn (fn [{:keys [args ret]}]
        (let [{:keys [vertex-a vertex-b]} args
              req-a (:vertex/req vertex-a)
              req-b (:vertex/req vertex-b)
              ret-req (:vertex/req ret)]
          ;; Invariant: Join required keys is exactly the union
          ;; and the join subsumes both parents
          (and (= ret-req (set/union req-a req-b))
               (pheme/subsumes? ret vertex-a)
               (pheme/subsumes? ret vertex-b)))))

(s/fdef pheme/derivation-chain-witness
  :args (s/cat :pipeline (s/with-gen (s/coll-of any? :kind vector?)
                           (fn [] (gen/return [(s/spec string?) (s/spec (fn [s] (> (count s) 1)))])))
               :referent (s/with-gen any? (fn [] (gen/elements ["hello" "a" "test-string"]))))
  :ret (s/keys :req [:witness/valid? :witness/initial :witness/steps])
  :fn (fn [{:keys [args ret]}]
        (let [{:keys [referent pipeline]} args]
          ;; Invariant: Initial referent is preserved, and step count equals completed steps
          (and (= referent (:witness/initial ret))
               (<= (count (:witness/steps ret)) (count pipeline))))))

(s/fdef pheme/retract-vertex
  :args (s/cat :claimed-vertex ::vertex
               :referent (s/with-gen map?
                           #(gen/map (s/gen :seme.feat/id)
                                     (gen/elements ["val" 42 true])
                                     {:max-elements 4})))
  :ret (s/keys :req [:retraction/claimed-vertex :retraction/retracted-vertex
                     :retraction/admitted-keys :retraction/falsified-keys
                     :retraction/fully-satisfied?])
  :fn (fn [{:keys [args ret]}]
        (let [{:keys [claimed-vertex]} args
              retracted (:retraction/retracted-vertex ret)]
          ;; Invariant: Claimed vertex subsumes retracted vertex (subsumption ordering)
          ;; and admitted keys union falsified keys equals claimed required keys
          (and (pheme/subsumes? claimed-vertex retracted)
               (= (:vertex/req claimed-vertex)
                  (set/union (:retraction/admitted-keys ret)
                             (:retraction/falsified-keys ret)))))))

(s/fdef pheme/lattice-provocation
  :args (s/cat :claimed-vertex ::vertex
               :referent (s/with-gen map?
                           #(gen/map (s/gen :seme.feat/id)
                                     (gen/elements ["val" 42 true])
                                     {:max-elements 4})))
  :ret (s/nilable (s/keys :req [:provocation/type :provocation/claimed-vertex
                               :provocation/retracted-vertex :provocation/provoked-axioms
                               :provocation/preserved-axioms]))
  :fn (fn [{:keys [args ret]}]
        (let [{:keys [claimed-vertex referent]} args]
          (if (nil? ret)
            (empty? (:retraction/falsified-keys (pheme/retract-vertex claimed-vertex referent)))
            (and (seq (:provocation/provoked-axioms ret))
                 (= (:provocation/claimed-vertex ret) (:vertex/id claimed-vertex :v/anonymous)))))))

;; --- Function Specs: Delome & Epistemic Habit Loop ---

(s/fdef delome/boundary-specimen?
  :args (s/cat :vertex-a ::vertex :vertex-b ::vertex :specimen map?)
  :ret boolean?)

(s/fdef delome/synthesize-boundary-specimen
  :args (s/or :two (s/cat :vertex-a ::vertex :vertex-b ::vertex)
              :three (s/cat :vertex-a ::vertex :vertex-b ::vertex :opts map?))
  :ret (s/keys :req-un [::found?])
  :fn (fn [{:keys [args ret]}]
        (let [v-a (if (= :two (first args)) (:vertex-a (second args)) (:vertex-a (second args)))
              v-b (if (= :two (first args)) (:vertex-b (second args)) (:vertex-b (second args)))]
          ;; Invariant: If a boundary specimen was found, it must satisfy boundary-specimen?
          (if (:found? ret)
            (delome/boundary-specimen? v-a v-b (:specimen ret))
            true))))

(s/fdef delome/rehydrate-unqualified
  :args (s/or :two (s/cat :raw-map map? :target-ns-or-dict any?)
              :three (s/cat :raw-map map? :target-ns-or-dict any? :opts map?))
  :ret (s/keys :req [:rehydrated/valid? :rehydrated/referent])
  :fn (fn [{:keys [args ret]}]
        (and (map? (:rehydrated/referent ret))
             (boolean? (:rehydrated/valid? ret)))))

(s/fdef delome/infer-candidate-spec
  :args (s/cat :corpus (s/with-gen (s/coll-of map? :kind vector?)
                         #(gen/vector (gen/map (gen/elements [:id :count :flag])
                                               (gen/elements ["str" 42 true])
                                               {:max-elements 3})
                                      1 5)))
  :ret (s/nilable (s/keys :req [:inferred/req :inferred/opt :inferred/preds]))
  :fn (fn [{:keys [args ret]}]
        (let [{:keys [corpus]} args]
          (if (empty? corpus)
            (nil? ret)
            (and (vector? (:inferred/req ret))
                 (vector? (:inferred/opt ret))
                 (map? (:inferred/preds ret)))))))

(s/fdef delome/contingent-prediction
  :args (s/cat :delome-spec ::test-spec-coord
               :encounter (s/with-gen any? #(gen/elements ["hello" 42 :key {:a 1}])))
  :ret (s/keys :req-un [::logically-necessary? ::status])
  :fn (fn [{:keys [ret]}]
        ;; Invariant: Logical necessity is strictly false (Humean contingency, SEME-008)
        (and (false? (:logically-necessary? ret))
             (contains? #{:habit-borne-out :habit-falsified} (:status ret)))))

(s/fdef delome/epistemic-cycle
  :args (s/or :three (s/cat :pheme-spec ::test-spec-coord
                            :delome-spec ::test-spec-coord
                            :raw-encounter (s/with-gen any? #(gen/elements ["hello" 42 :key {:a 1}])))
              :four (s/cat :pheme-spec ::test-spec-coord
                           :delome-spec ::test-spec-coord
                           :raw-encounter (s/with-gen any? #(gen/elements ["hello" 42 :key {:a 1}]))
                           :opts (s/with-gen map? #(gen/return {}))))
  :ret (s/keys :req-un [::stage ::status])
  :fn (fn [{:keys [ret]}]
        (contains? #{:phemic-predication-failed
                     :contingent-prediction-falsified
                     :delomic-habit-confirmed}
                   (:stage ret))))

(s/fdef delome/pragmatic-conditional
  :args (s/cat :stimulus-spec ::test-spec-coord
               :action-fn ::action-fn
               :consequence-spec ::test-spec-coord
               :stimulus (s/with-gen any? #(gen/elements ["stimulus" 100 {:val 1}])))
  :ret (s/keys :req [:would-be/holds? :would-be/reason])
  :fn (fn [{:keys [ret]}]
        (boolean? (:would-be/holds? ret))))

(s/fdef pheme/retrograde-ancestor-trace
  :args (s/cat :witness (s/with-gen any?
                          #(gen/return {:witness/valid? false
                                        :witness/initial {:count 10}
                                        :witness/final nil
                                        :witness/steps [{:step/index 0 :step/predicate :p0 :step/input {:count 10} :step/output {:count 20} :step/valid? true}
                                                        {:step/index 1 :step/predicate :p1 :step/input {:count 20} :step/output nil :step/valid? false}]
                                        :witness/failed-step 1}))
               :failing-path (s/with-gen any? #(gen/return :count)))
  :ret (s/keys :req [:ancestor/found?])
  :fn (fn [{:keys [ret]}]
        (boolean? (:ancestor/found? ret))))

(s/fdef delome/disentangle-provocation-tier
  :args (s/or :two (s/cat :coord (s/with-gen any? #(gen/elements [:test/a :test/b "unnamespaced" 42]))
                          :val (s/with-gen any? #(gen/elements ["foo" 42 :bar])))
              :three (s/cat :coord (s/with-gen any? #(gen/elements [:test/a :test/b "unnamespaced" 42]))
                            :val (s/with-gen any? #(gen/elements ["foo" 42 :bar]))
                            :overlay-rules (s/with-gen any? #(gen/return {:test/a (fn [v] (string? v))}))))
  :ret (s/keys :req [:tier/classification :tier/coord :tier/val :tier/target-subsystem])
  :fn (fn [{:keys [ret]}]
        (contains? #{:tier/perimeter-ingress-shock
                     :tier/constitutive-invariant
                     :tier/selectional-restriction
                     :tier/sound}
                   (:tier/classification ret))))

;; --- Function Specs: Chain Grammars (CLJ-007) ---

(s/fdef chain/chain-grammar
  :args (s/cat :id keyword? :slots (s/coll-of ::slot :kind vector?))
  :ret ::chain-grammar
  :fn (fn [{:keys [args ret]}]
        (and (= (:id args) (:grammar/id ret))
             (= (:slots args) (:grammar/slots ret)))))

(s/fdef chain/conformed-chain-witness
  :args (s/cat :grammar ::chain-grammar
               :tokens (s/with-gen (s/coll-of any? :kind vector?)
                         #(gen/vector (gen/elements ["word" :tok 42]) 0 3)))
  :ret (s/keys :req [:chain/valid? :chain/grammar :chain/tokens :chain/parsed])
  :fn (fn [{:keys [args ret]}]
        (and (= (:tokens args) (:chain/tokens ret))
             (= (:grammar/id (:grammar args)) (:chain/grammar ret)))))
