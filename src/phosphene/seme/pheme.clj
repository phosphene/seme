(ns phosphene.seme.pheme
  "Peirce's Secondness: Perceptual judgment, conformance, and multi-vertex gating.
   Evaluates open referent maps against sovereign specs and feature bundles,
   producing structured proof-carrying witnesses without boolean blindness."
  (:require [clojure.spec.alpha :as s]))

;; --- Proof-Carrying Witness ---

(defn conformed-witness
  "Evaluates referent against spec-k.
   Returns a proof-carrying witness record containing:
     - :witness/valid?  - boolean conformance result
     - :witness/parsed  - structured parse AST if valid, nil if invalid
     - :witness/anatomy - symbolic form of the spec (s/form)
     - :witness/anomaly - clojure.spec.alpha/explain-data if invalid, nil if valid
     - :witness/referent - the original unmutated referent."
  [spec-k referent]
  (let [valid? (s/valid? spec-k referent)]
    {:witness/spec spec-k
     :witness/valid? valid?
     :witness/parsed (when valid? (s/conform spec-k referent))
     :witness/anatomy (s/form spec-k)
     :witness/anomaly (when-not valid? (s/explain-data spec-k referent))
     :witness/referent referent}))

(defn witness?
  "Returns true if x is a valid proof-carrying witness record."
  [x]
  (and (map? x)
       (contains? x :witness/valid?)
       (contains? x :witness/anatomy)
       (contains? x :witness/referent)))

;; --- Multi-Vertex Projection over Open Referents ---

(defn admits-vertex?
  "Checks if an open-world referent map satisfies the required coordinates of vertex-spec."
  [vertex-spec referent]
  (s/valid? vertex-spec referent))

(defn project-vertices
  "Projects a collection of orthogonal analytical vertices onto an open-world referent.
   vertices is a map of {vertex-id spec-or-keyset}.
   Returns a map of {vertex-id witness} for each vertex, demonstrating how
   disparate perspectives (phonological, morphosyntactic, historical) co-exist on one map."
  [vertices referent]
  (reduce-kv
    (fn [acc v-id v-spec]
      (assoc acc v-id (conformed-witness v-spec referent)))
    {}
    vertices))

;; --- Diagnostic Anomaly Extraction ---

(defn explain-anomaly
  "Extracts a clean, machine-actionable anomaly summary from an invalid witness."
  [witness]
  (when-not (:witness/valid? witness)
    (let [ed (:witness/anomaly witness)
          problems (get ed :clojure.spec.alpha/problems [])]
      {:anomaly/spec (:witness/spec witness)
       :anomaly/problem-count (count problems)
       :anomaly/paths (mapv :path problems)
       :anomaly/predicates (mapv :pred problems)})))
