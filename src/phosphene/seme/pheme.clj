(ns phosphene.seme.pheme
  "Peirce's Secondness: Perceptual judgment, conformance, and multi-vertex gating.
   Evaluates open referent maps against sovereign specs and feature bundles,
   producing structured proof-carrying witnesses without boolean blindness.
   Implements a formal bounded vertex lattice over analytical keysets (CLJ-002)."
  (:require [clojure.set :as set]
            [clojure.spec.alpha :as s]))

;; --- Proof-Carrying Witness ---

(defn conformed-witness
  "Evaluates referent against spec-k.
   Returns a proof-carrying witness record containing:
     - :witness/spec     - sovereign spec coordinate
     - :witness/valid?   - boolean conformance result
     - :witness/parsed   - structured parse AST if valid, nil if invalid
     - :witness/anatomy  - symbolic form of the spec (s/form)
     - :witness/anomaly  - clojure.spec.alpha/explain-data if invalid, nil if valid
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

;; --- Analytical Vertex Specification & Keyset Extraction ---

(defn vertex
  "Constructs an analytical vertex record defining required coordinates and optional predicates.
   id: Sovereign keyword identifier (e.g. :seme.vertex/phonology).
   req-keys: Set or vector of required namespaced coordinate keywords.
   opt-keys: Optional set or vector of optional coordinate keywords.
   metadata: Optional descriptive metadata map."
  ([id req-keys]
   (vertex id req-keys [] {}))
  ([id req-keys opt-keys]
   (vertex id req-keys opt-keys {}))
  ([id req-keys opt-keys metadata]
   {:vertex/id id
    :vertex/req (set req-keys)
    :vertex/opt (set opt-keys)
    :vertex/metadata metadata}))

(defn vertex?
  "Returns true if x is a valid analytical vertex record."
  [x]
  (and (map? x)
       (keyword? (:vertex/id x))
       (set? (:vertex/req x))
       (set? (:vertex/opt x))))

(defn vertex-req-keys
  "Extracts the set of required coordinate keys from a vertex record or s/keys spec form."
  [v]
  (cond
    (vertex? v) (:vertex/req v)
    (keyword? v) (let [form (s/form v)]
                   (if (and (seq? form) (= 'clojure.spec.alpha/keys (first form)))
                     (let [keys-map (apply hash-map (rest form))]
                       (set (:req keys-map)))
                     #{}))
    :else #{}))

;; --- Multi-Vertex Projection over Open Referents ---

(defn admits-vertex?
  "Checks if an open-world referent map satisfies the required coordinates of vertex.
   vertex can be an analytical vertex record or an s/keys spec coordinate."
  [v referent]
  (cond
    (vertex? v) (and (map? referent)
                     (set/subset? (:vertex/req v) (set (keys referent)))
                     (every? (fn [k]
                               (if (s/get-spec k)
                                 (s/valid? k (get referent k))
                                 true))
                             (:vertex/req v)))
    (keyword? v) (s/valid? v referent)
    :else false))

(defn project-vertices
  "Projects a collection of orthogonal analytical vertices onto an open-world referent.
   vertices is a map of {vertex-id vertex-spec-or-record}.
   Returns a map of {vertex-id witness} for each vertex, demonstrating how
   disparate perspectives (phonological, morphosyntactic, historical) co-exist on one map."
  [vertices referent]
  (reduce-kv
    (fn [acc v-id v-spec]
      (if (vertex? v-spec)
        (let [valid? (admits-vertex? v-spec referent)]
          (assoc acc v-id {:witness/spec (:vertex/id v-spec)
                           :witness/valid? valid?
                           :witness/parsed (when valid? (select-keys referent (set/union (:vertex/req v-spec) (:vertex/opt v-spec))))
                           :witness/anatomy (:vertex/req v-spec)
                           :witness/anomaly (when-not valid? {:missing (set/difference (:vertex/req v-spec) (set (keys referent)))})
                           :witness/referent referent}))
        (assoc acc v-id (conformed-witness v-spec referent))))
    {}
    vertices))

;; --- Vertex Lattice & Keyset Subsumption Algebra (CLJ-002) ---

(defn subsumes?
  "Evaluates whether vertex-a subsumes vertex-b (Va >= Vb).
   In the keyset membership lattice, Va subsumes Vb iff the coordinates required by Vb
   are a subset of the coordinates required by Va: ReqKeys(Vb) subset-of ReqKeys(Va).
   Thus, any referent map satisfying Va is guaranteed to satisfy Vb."
  [vertex-a vertex-b]
  (let [req-a (vertex-req-keys vertex-a)
        req-b (vertex-req-keys vertex-b)]
    (set/subset? req-b req-a)))

(defn vertex-meet
  "Computes the greatest lower bound (Meet, /\\) of two analytical vertices.
   The meet represents the minimal shared consensus—the intersection of required coordinates.
   Any referent satisfying either vertex will satisfy the meet."
  [vertex-a vertex-b]
  (let [id-a (:vertex/id vertex-a (if (keyword? vertex-a) vertex-a :v/a))
        id-b (:vertex/id vertex-b (if (keyword? vertex-b) vertex-b :v/b))
        meet-id (keyword (namespace id-a) (str (name id-a) "-meet-" (name id-b)))
        req-a (vertex-req-keys vertex-a)
        req-b (vertex-req-keys vertex-b)
        opt-a (:vertex/opt vertex-a #{})
        opt-b (:vertex/opt vertex-b #{})]
    (vertex meet-id
            (set/intersection req-a req-b)
            (set/union opt-a opt-b)
            {:lattice/op :meet :lattice/operands [id-a id-b]})))

(defn vertex-join
  "Computes the least upper bound (Join, \\/) of two analytical vertices.
   The join represents the composite unification—the union of required coordinates.
   A referent must simultaneously satisfy both disciplines to satisfy the join."
  [vertex-a vertex-b]
  (let [id-a (:vertex/id vertex-a (if (keyword? vertex-a) vertex-a :v/a))
        id-b (:vertex/id vertex-b (if (keyword? vertex-b) vertex-b :v/b))
        join-id (keyword (namespace id-a) (str (name id-a) "-join-" (name id-b)))
        req-a (vertex-req-keys vertex-a)
        req-b (vertex-req-keys vertex-b)
        opt-a (:vertex/opt vertex-a #{})
        opt-b (:vertex/opt vertex-b #{})]
    (vertex join-id
            (set/union req-a req-b)
            (set/difference (set/union opt-a opt-b) (set/union req-a req-b))
            {:lattice/op :join :lattice/operands [id-a id-b]})))

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
