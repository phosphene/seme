(ns phosphene.seme.spec
  "Formal specification and property invariants for phosphene.seme."
  (:require [clojure.spec.alpha :as s]
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

;; --- Function Specs ---

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
          (= (clojure.set/union a-ids b-ids)
             (clojure.set/union (:only-a ret) (:only-b ret) (:shared ret))))))

(s/fdef pheme/conformed-witness
  :args (s/cat :spec-k (s/with-gen any? #(gen/return any?))
               :referent (s/with-gen any? #(gen/return {:sample/id "id-1"})))
  :ret ::witness
  :fn (fn [{:keys [args ret]}]
        (let [{:keys [referent]} args]
          ;; Invariant: Referent is preserved without mutation
          (= referent (:witness/referent ret)))))

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
