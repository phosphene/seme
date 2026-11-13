(ns phosphene.seme.spec
  "Formal specification and property invariants for phosphene.seme."
  (:require [clojure.set :as set]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [phosphene.seme.rheme :as rheme]
            [phosphene.seme.pheme :as pheme]
            [phosphene.seme.delome :as delome]))

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

;; --- Function Specs: Delome ---

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
        (let [raw-map (if (= :two (first args)) (:raw-map (second args)) (:raw-map (second args)))]
          ;; Invariant: Rehydrated map must be a map
          (and (map? (:rehydrated/referent ret))
               (boolean? (:rehydrated/valid? ret))))))

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
