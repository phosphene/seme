(ns phosphene.seme.complementarity
  "Bohrian Non-Commutative Complementarity, Wittgensteinian Hinge Sovereignty,
   and Anti-Collapse Invariants (T-SEM-013).
   Formalizes:
     - Complementary dyads of incommensurable analytical vertices (SEME-COMP-001)
     - Operational non-commutative commutators [T_A, T_B](x) != 0 (SEME-COMP-002)
     - Anti-collapse gates forbidding lossy join unifications (SEME-COMP-003)
     - Wittgensteinian hinge sovereignty over two-segment namespaces (SEME-COMP-004)
     - Bohrian dual-aspect proof witnesses preserving referent purity (SEME-COMP-005)
     - Structural incommensurability dossiers isolating coordinate divergence (SEME-COMP-006)."
  (:require [clojure.set :as set]
            [clojure.spec.alpha :as s]
            [phosphene.seme.pheme :as pheme]))

;; --- SEME-COMP-001: Complementary Dyad Constructor ---

(defn complementary-dyad
  "Constructs a complementary dyad record binding two sovereign analytical vertices
   that describe the same empirical referent under mutually exclusive or scale-separated
   experimental or conceptual conditions.
   dyad-id: sovereign keyword identifier (e.g. :dyad/mechanics or :dyad/evolution)
   vertex-a: first analytical vertex (e.g. Newtonian mechanics or micro allele frequency)
   vertex-b: second analytical vertex (e.g. Relativistic mechanics or cladistic macro-evolution)
   opts: optional metadata map specifying :incommensurable? (default true),
         :domain (e.g. :kinematics), :scale-separation (e.g. :meso-vs-relativistic)."
  ([dyad-id vertex-a vertex-b]
   (complementary-dyad dyad-id vertex-a vertex-b {}))
  ([dyad-id vertex-a vertex-b opts]
   {:dyad/id dyad-id
    :dyad/vertex-a vertex-a
    :dyad/vertex-b vertex-b
    :dyad/incommensurable? (get opts :incommensurable? true)
    :dyad/domain (:domain opts)
    :dyad/scale-separation (:scale-separation opts)
    :dyad/metadata opts}))

(defn complementary-dyad?
  "Returns true if x is a valid complementary dyad record."
  [x]
  (and (map? x)
       (keyword? (:dyad/id x))
       (contains? x :dyad/vertex-a)
       (contains? x :dyad/vertex-b)
       (boolean? (:dyad/incommensurable? x))))

;; --- SEME-COMP-004: Wittgensteinian Hinge Sovereignty ---

(defn extract-hinge-namespace
  "Extracts the hinge namespace from a coordinate keyword or vertex identifier.
   In accordance with Axiom A4 and CLJ-015, the namespace fixes the language-game's
   conditions of possibility, preventing coordinate conflation across paradigms."
  [coord]
  (cond
    (keyword? coord) (keyword (namespace coord))
    (map? coord) (when-let [v-id (:vertex/id coord)]
                   (keyword (namespace v-id)))
    :else nil))

(defn sovereign-hinges-distinct?
  "Returns true if two vertices or coordinates originate from orthogonal,
   distinct hinge namespaces."
  [vertex-a vertex-b]
  (let [h-a (extract-hinge-namespace vertex-a)
        h-b (extract-hinge-namespace vertex-b)]
    (and (some? h-a)
         (some? h-b)
         (not= h-a h-b))))

;; --- SEME-COMP-002: Operational Commutator [T_A, T_B](x) ---

(defn commutator
  "Evaluates the operational commutator between two sequential operations or
   derivation chains (transduction-a and transduction-b) applied to referent:
     [T_A, T_B](x) = (T_B o T_A)(x) - (T_A o T_B)(x)
   transduction-a, transduction-b: fn [ref] -> ref' (or derivation pipeline vectors).
   Returns a structured commutator record:
     {:commutator/zero? boolean
      :commutator/ab-result (T_B (T_A x))
      :commutator/ba-result (T_A (T_B x))
      :commutator/residual map-of-differences-between-ab-and-ba}."
  [transduction-a transduction-b referent]
  (let [apply-trans (fn [trans ref]
                      (cond
                        (fn? trans) (trans ref)
                        (vector? trans) (:witness/final (pheme/derivation-chain-witness trans ref))
                        :else ref))
        ab (apply-trans transduction-b (apply-trans transduction-a referent))
        ba (apply-trans transduction-a (apply-trans transduction-b referent))
        zero? (= ab ba)
        diff (if zero?
               {}
               (cond
                 (and (map? ab) (map? ba))
                 (let [all-keys (set/union (set (keys ab)) (set (keys ba)))]
                   (reduce (fn [acc k]
                             (let [v-ab (get ab k)
                                   v-ba (get ba k)]
                               (if (not= v-ab v-ba)
                                 (assoc acc k {:ab v-ab :ba v-ba})
                                 acc)))
                           {}
                           all-keys))
                 :else {:ab ab :ba ba}))]
    {:commutator/zero? zero?
     :commutator/ab-result ab
     :commutator/ba-result ba
     :commutator/residual diff}))

;; --- SEME-COMP-003: Anti-Collapse Invariant ---

(defn incommensurable?
  "Returns true if the dyad represents an incommensurable complementary pair."
  [dyad]
  (true? (:dyad/incommensurable? dyad)))

(defn assert-non-collapsible!
  "Anti-collapse invariant gate. Forbids the system from performing a naive lossy
   unification or poset join on an incommensurable complementary dyad.
   Throws an ex-info if the dyad is declared incommensurable, protecting the
   distinct descriptive power of both paradigms."
  [dyad]
  (when (incommensurable? dyad)
    (let [v-a (:dyad/vertex-a dyad)
          v-b (:dyad/vertex-b dyad)
          id-a (if (map? v-a) (:vertex/id v-a) v-a)
          id-b (if (map? v-b) (:vertex/id v-b) v-b)]
      (throw (ex-info (str "Anti-Collapse Invariant Breach: Cannot compute naive join or "
                           "universal spec unification on incommensurable complementary dyad "
                           (:dyad/id dyad) " between [" id-a "] and [" id-b "]. "
                           "Frameworks do not commute; use dual-aspect-witness or cross-scale projection.")
                      {:anomaly/type :anti-collapse-invariant-violation
                       :dyad/id (:dyad/id dyad)
                       :vertex/a id-a
                       :vertex/b id-b})))))

;; --- SEME-COMP-006: Structural Incommensurability Dossier ---

(defn incommensurability-dossier
  "Extracts a structural diagnostic dossier detailing the incommensurable gap between
   vertex-a and vertex-b:
     - :dossier/only-a-keys        required coordinates unique to vertex-a
     - :dossier/only-b-keys        required coordinates unique to vertex-b
     - :dossier/shared-keys        coordinates shared across both vertices
     - :dossier/hinge-namespaces   distinct hinge namespaces involved
     - :dossier/incommensurable?   boolean confirmation."
  [dyad]
  (let [v-a (:dyad/vertex-a dyad)
        v-b (:dyad/vertex-b dyad)
        req-a (pheme/vertex-req-keys v-a)
        req-b (pheme/vertex-req-keys v-b)
        h-a (extract-hinge-namespace v-a)
        h-b (extract-hinge-namespace v-b)]
    {:dossier/dyad-id (:dyad/id dyad)
     :dossier/only-a-keys (set/difference req-a req-b)
     :dossier/only-b-keys (set/difference req-b req-a)
     :dossier/shared-keys (set/intersection req-a req-b)
     :dossier/hinge-namespaces (disj (set (remove nil? [h-a h-b])) nil)
     :dossier/incommensurable? (incommensurable? dyad)}))

;; --- SEME-COMP-005: Bohrian Dual-Aspect Proof Witness ---

(defn dual-aspect-witness
  "Evaluates a single empirical referent simultaneously through both lenses of a
   complementary dyad.
   Preserves the pristine referent, producing a proof-carrying dual witness containing:
     - :witness/dyad-id        id of the complementary dyad
     - :witness/aspect-a       conformed witness under vertex-a lens
     - :witness/aspect-b       conformed witness under vertex-b lens
     - :witness/both-valid?    true iff referent satisfies both regimes (correspondence limit)
     - :witness/regime         :both-hold, :aspect-a-only, :aspect-b-only, or :neither-holds
     - :witness/referent       original unmutated referent."
  ([dyad referent]
   (dual-aspect-witness dyad referent nil nil))
  ([dyad referent trans-a trans-b]
   (let [v-a (:dyad/vertex-a dyad)
         v-b (:dyad/vertex-b dyad)
         wit-a (if (map? v-a)
                 (let [valid? (pheme/admits-vertex? v-a referent)]
                   {:witness/spec (:vertex/id v-a)
                    :witness/valid? valid?
                    :witness/parsed (when valid? (select-keys referent (:vertex/req v-a)))
                    :witness/anatomy (:vertex/req v-a)
                    :witness/referent referent})
                 (pheme/conformed-witness v-a referent))
         wit-b (if (map? v-b)
                 (let [valid? (pheme/admits-vertex? v-b referent)]
                   {:witness/spec (:vertex/id v-b)
                    :witness/valid? valid?
                    :witness/parsed (when valid? (select-keys referent (:vertex/req v-b)))
                    :witness/anatomy (:vertex/req v-b)
                    :witness/referent referent})
                 (pheme/conformed-witness v-b referent))
         val-a? (:witness/valid? wit-a)
         val-b? (:witness/valid? wit-b)
         regime (cond
                  (and val-a? val-b?) :both-hold
                  (and val-a? (not val-b?)) :aspect-a-only
                  (and (not val-a?) val-b?) :aspect-b-only
                  :else :neither-holds)
         comm (when (and trans-a trans-b)
                (commutator trans-a trans-b referent))]
     (cond-> {:witness/dyad-id (:dyad/id dyad)
              :witness/aspect-a wit-a
              :witness/aspect-b wit-b
              :witness/both-valid? (and val-a? val-b?)
              :witness/regime regime
              :witness/referent referent}
       comm (assoc :witness/commutator comm)))))
