(ns phosphene.seme.delome
  "Peirce's Thirdness: Inferential habits, selectional restrictions,
   contextual overlays, and inductive spec distillation.
   Bridges top-down deductive specifications with bottom-up empirical Mode-S data."
  (:require [clojure.spec.alpha :as s]))

;; --- Contextual Overlays for Selectional Restrictions ---

(defn contextual-overlay
  "Constructs a contextual overlay that wraps a base spec registry with
   vertex-local selectional restrictions without mutating global coordinates.
   base-registry: map of {coord spec}
   overlay-rules: map of {coord specialized-spec}"
  [base-registry overlay-rules]
  (merge base-registry overlay-rules))

(defn evaluate-overlay
  "Evaluates referent against a contextual overlay registry."
  [overlay coord val]
  (if-let [pred (get overlay coord)]
    (boolean (pred val))
    false))

;; --- Inductive Habit Distillation (Mode-S -> Mode-D Bridge) ---

(defn infer-primitive-spec
  "Infers a basic predicate identifier from an observed empirical Clojure value."
  [val]
  (cond
    (integer? val) 'clojure.core/integer?
    (number? val)  'clojure.core/number?
    (string? val)  'clojure.core/string?
    (keyword? val) 'clojure.core/keyword?
    (boolean? val) 'clojure.core/boolean?
    (vector? val)  'clojure.core/vector?
    (set? val)     'clojure.core/set?
    (map? val)     'clojure.core/map?
    :else          'clojure.core/any?))

(defn infer-candidate-spec
  "Inductively distills a candidate schema definition from a Mode-S empirical corpus.
   Given a collection of referent maps, identifies consistently present keys and their
   most specific observed predicate habits."
  [corpus]
  (when (seq corpus)
    (let [all-keys (set (mapcat keys corpus))
          n (count corpus)
          ;; A key is required if present in 100% of corpus specimens
          req-keys (filterv (fn [k] (= n (count (filter #(contains? % k) corpus)))) all-keys)
          opt-keys (filterv (fn [k] (< (count (filter #(contains? % k) corpus)) n)) all-keys)
          ;; Infer candidate predicate per key
          key-preds (reduce
                      (fn [acc k]
                        (let [observed-vals (keep #(get % k) corpus)
                              preds (distinct (map infer-primitive-spec observed-vals))]
                          (assoc acc k (if (= 1 (count preds)) (first preds) 'clojure.core/any?))))
                      {}
                      all-keys)]
      {:inferred/req req-keys
       :inferred/opt opt-keys
       :inferred/preds key-preds})))

;; --- Differential Falsification ---

(defn boundary-specimen?
  "Checks if specimen is a boundary specimen that satisfies vertex-a but falsifies vertex-b."
  [vertex-a vertex-b specimen]
  (and (s/valid? vertex-a specimen)
       (not (s/valid? vertex-b specimen))))
