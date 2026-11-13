(ns phosphene.seme.pheme
  "Peirce's Secondness: Perceptual judgment, conformance, and multi-vertex gating.
   Evaluates open referent maps against sovereign specs and feature bundles,
   producing structured proof-carrying witnesses without boolean blindness.
   Implements a formal bounded vertex lattice over analytical keysets (CLJ-002),
   derivation-chain witnessing for multi-stage s/and conjunction pipelines (CLJ-001-A, CLJ-006),
   and Poset Lattice Retraction with Structural Provocation (T-SEM-009)."
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

;; --- Derivation-Chain Witnessing (CLJ-001-A, CLJ-006, SEME-ARCH-003) ---

(defn derivation-chain-witness
  "Executes a multi-stage sequential predication and conformance pipeline over referent.
   pipeline is a sequence of spec coordinates or predicate functions [s1 s2 ... sn].
   In accordance with CLJ-001-A (conjunction re-hydration), each stage receives the conformed
   output of the preceding stage:
     referent -> stage_1 -> stage_2 -> ... -> stage_n
   Returns an auditable derivation witness record:
     {:witness/valid?       boolean
      :witness/initial      unmutated input referent
      :witness/final        conformed output of final stage (or nil if failed)
      :witness/steps        vector of step records:
                             [{:step/index i
                               :step/predicate pred-or-spec
                               :step/input in
                               :step/output out
                               :step/valid? bool
                               :step/anomaly explain-data}]
      :witness/failed-step  index of failing step (or nil if all succeeded)}"
  [pipeline referent]
  (loop [remaining pipeline
         idx 0
         curr-val referent
         steps []]
    (if (empty? remaining)
      {:witness/valid? true
       :witness/initial referent
       :witness/final curr-val
       :witness/steps steps
       :witness/failed-step nil}
      (let [stage (first remaining)
            spec-like? (or (keyword? stage) (s/spec? stage))
            valid? (if spec-like?
                     (s/valid? stage curr-val)
                     (boolean (stage curr-val)))
            conformed (when valid?
                        (if spec-like?
                          (s/conform stage curr-val)
                          (stage curr-val)))
            anomaly (when-not valid?
                      (if spec-like?
                        (s/explain-data stage curr-val)
                        {:clojure.spec.alpha/problems [{:path [] :pred stage :val curr-val}]}))
            step-record {:step/index idx
                         :step/predicate (if (keyword? stage) stage (str stage))
                         :step/input curr-val
                         :step/output conformed
                         :step/valid? valid?
                         :step/anomaly anomaly}]
        (if valid?
          (recur (rest remaining)
                 (inc idx)
                 conformed
                 (conj steps step-record))
          {:witness/valid? false
           :witness/initial referent
           :witness/final nil
           :witness/steps (conj steps step-record)
           :witness/failed-step idx})))))

(defn derivation-witness?
  "Returns true if x is a valid derivation-chain witness record."
  [x]
  (and (map? x)
       (contains? x :witness/valid?)
       (contains? x :witness/initial)
       (contains? x :witness/steps)
       (vector? (:witness/steps x))))

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

;; --- Poset Lattice Retraction & Structural Provocation (T-SEM-009) ---

(defn retract-vertex
  "Evaluates an open referent map against a claimed analytical vertex.
   If the referent satisfies all required coordinates, the claimed vertex stands.
   If any required coordinate is missing or invalid, retracts to the maximal sub-vertex
   (lattice meet) that remains true under the encounter.
   Returns:
     {:retraction/claimed-vertex vertex
      :retraction/retracted-vertex retracted-sub-vertex
      :retraction/admitted-keys set-of-satisfied-coordinates
      :retraction/falsified-keys set-of-falsified-coordinates
      :retraction/anomalies map-of-{falsified-coord explain-data-or-:missing}
      :retraction/fully-satisfied? boolean}."
  [claimed-vertex referent]
  (let [req-keys (vertex-req-keys claimed-vertex)
        opt-keys (:vertex/opt claimed-vertex #{})
        ref-keys (if (map? referent) (set (keys referent)) #{})
        v-id (:vertex/id claimed-vertex :v/anonymous)
        ;; Partition req-keys into admitted and falsified
        evaluation (reduce
                     (fn [acc k]
                       (if-not (contains? ref-keys k)
                         (-> acc
                             (update :falsified conj k)
                             (assoc-in [:anomalies k] :missing-key))
                         (let [v (get referent k)
                               spec (s/get-spec k)]
                           (if (and spec (not (s/valid? spec v)))
                             (-> acc
                                 (update :falsified conj k)
                                 (assoc-in [:anomalies k] (s/explain-data spec v)))
                             (update acc :admitted conj k)))))
                     {:admitted #{} :falsified #{} :anomalies {}}
                     req-keys)
        admitted-keys (:admitted evaluation)
        falsified-keys (:falsified evaluation)
        anomalies (:anomalies evaluation)
        fully-satisfied? (empty? falsified-keys)
        retracted-id (if fully-satisfied?
                       v-id
                       (keyword (namespace v-id) (str (name v-id) "-retracted")))
        retracted-v (vertex retracted-id
                            admitted-keys
                            opt-keys
                            {:retraction/parent v-id
                             :retraction/loss falsified-keys})]
    {:retraction/claimed-vertex claimed-vertex
     :retraction/retracted-vertex retracted-v
     :retraction/admitted-keys admitted-keys
     :retraction/falsified-keys falsified-keys
     :retraction/anomalies (when-not fully-satisfied? anomalies)
     :retraction/fully-satisfied? fully-satisfied?}))

(defn lattice-provocation
  "Computes the formal axiomatic provocation generated when an empirical encounter
   falsifies one or more required leaf predicates of claimed-vertex.
   Returns nil if fully satisfied.
   If falsified, returns a structured provocation proof object:
     {:provocation/type :structural-requirement-falsification
      :provocation/claimed-vertex v-id
      :provocation/retracted-vertex retracted-v-id
      :provocation/provoked-axioms set-of-falsified-coordinates
      :provocation/preserved-axioms set-of-admitted-coordinates
      :provocation/retraction retraction-record}."
  [claimed-vertex referent]
  (let [ret (retract-vertex claimed-vertex referent)]
    (when-not (:retraction/fully-satisfied? ret)
      {:provocation/type :structural-requirement-falsification
       :provocation/claimed-vertex (:vertex/id claimed-vertex :v/anonymous)
       :provocation/retracted-vertex (:vertex/id (:retraction/retracted-vertex ret))
       :provocation/provoked-axioms (:retraction/falsified-keys ret)
       :provocation/preserved-axioms (:retraction/admitted-keys ret)
       :provocation/retraction ret})))

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
