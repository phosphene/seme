(ns phosphene.seme.chain
  "Sequential Predicative Grammars & Syntagma Interoperability (CLJ-007, CLJ-007-C/A/R).
   Composes horizontal syntagmatic chaining (linear order sigma_1 prec sigma_2 prec sigma_3)
   with internal distinctive semantic features (semes and sememes).
   Parses notation vectors against regex grammars where sequence positions are constrained
   by unsaturated feature predicates, labeled alternatives (s/alt first-match habits),
   and scoped hierarchical nesting without token flattening (T-SEM-014)."
  (:require [clojure.spec.alpha :as s]
            [clojure.set :as set]
            [phosphene.seme.rheme :as rheme]))

;; --- Grammar Slot Specification ---

(defn slot
  "Defines a grammar slot constrained by an unsaturated seme predicate or spec coordinate.
   name-k: Tag keyword for conformed parse tree (e.g. :subject, :verb, :object).
   pred-or-seme: Seme record, spec keyword, or predicate function.
   cardinality: One of :one (default), :opt, :star, :plus."
  ([name-k pred-or-seme]
   (slot name-k pred-or-seme :one))
  ([name-k pred-or-seme cardinality]
   {:slot/name name-k
    :slot/type :predicate
    :slot/pred (if (rheme/seme? pred-or-seme)
                 (:seme/pred pred-or-seme)
                 pred-or-seme)
    :slot/cardinality cardinality}))

(defn alt-slot
  "Defines a labeled alternative slot (CLJ-007-ALT) evaluated via deterministic
   first-match priority per Hickey habits.
   name-k: Tag keyword for conformed parse tree.
   branches: Ordered vector of [branch-tag pred-or-seme] pairs.
             e.g. [[:citation (fn [b] (= :blockquote (:block-type b)))]
                   [:code     (fn [b] (= :code (:block-type b)))]]
   cardinality: One of :one (default), :opt, :star, :plus."
  ([name-k branches]
   (alt-slot name-k branches :one))
  ([name-k branches cardinality]
   {:slot/name name-k
    :slot/type :alt
    :slot/branches (mapv (fn [[tag pred]]
                           [tag (if (rheme/seme? pred) (:seme/pred pred) pred)])
                         branches)
    :slot/cardinality cardinality}))

(defn nested-slot
  "Defines a scoped hierarchical sub-grammar slot (CLJ-007-NEST) that matches
   nested token sequences without token stream flattening.
   name-k: Tag keyword for conformed parse tree.
   child-grammar: chain-grammar record for the nested sub-sequence.
   cardinality: One of :one (default), :opt, :star, :plus."
  ([name-k child-grammar]
   (nested-slot name-k child-grammar :one))
  ([name-k child-grammar cardinality]
   {:slot/name name-k
    :slot/type :nested
    :slot/child-grammar child-grammar
    :slot/cardinality cardinality}))

(defn slot?
  "Returns true if x is a valid grammar slot."
  [x]
  (and (map? x)
       (keyword? (:slot/name x))
       (contains? #{:predicate :alt :nested} (get x :slot/type :predicate))
       (contains? #{:one :opt :star :plus} (:slot/cardinality x))))

(defn compile-slot-spec
  "Compiles a slot specification into a clojure.spec.alpha regex operator."
  [slot-def]
  (let [p (:slot/pred slot-def)
        pred-fn (cond
                  (keyword? p) p
                  (fn? p) p
                  :else (constantly true))]
    (case (:slot/cardinality slot-def)
      :one  pred-fn
      :opt  (s/? pred-fn)
      :star (s/* pred-fn)
      :plus (s/+ pred-fn))))

;; --- Chain Grammar Construction ---

(defn chain-grammar
  "Constructs a sequential predicative grammar from an ordered vector of slot specifications.
   Returns a grammar record containing the slot definitions."
  [id slots]
  {:grammar/id id
   :grammar/slots (vec slots)})

(defn chain-grammar?
  "Returns true if x is a valid chain grammar record."
  [x]
  (and (map? x)
       (keyword? (:grammar/id x))
       (vector? (:grammar/slots x))
       (every? slot? (:grammar/slots x))))

;; --- Slot Matching & Conformance Evaluators ---

(declare conformed-chain-witness)

(defn match-predicate?
  "Tests whether a token or referent item satisfies a predicate or spec coordinate."
  [p item]
  (cond
    (keyword? p) (s/valid? p item)
    (fn? p) (boolean (p item))
    (s/spec? p) (s/valid? p item)
    :else false))

(defn eval-slot-item
  "Evaluates a single item against a slot definition.
   Returns nil if no match, or the conformed item value (tagged for :alt, witnessed for :nested)."
  [slot-def item]
  (case (get slot-def :slot/type :predicate)
    :predicate
    (when (match-predicate? (:slot/pred slot-def) item)
      item)

    :alt
    ;; Hickey habit: deterministic first-match priority across declared branches
    (loop [branches (seq (:slot/branches slot-def))]
      (if (empty? branches)
        nil
        (let [[tag pred] (first branches)]
          (if (match-predicate? pred item)
            [tag item]
            (recur (rest branches))))))

    :nested
    ;; Nested slot matches a collection/vector against child grammar
    (when (or (vector? item) (seq? item))
      (let [child-witness (conformed-chain-witness (:slot/child-grammar slot-def) item)]
        (when (:chain/valid? child-witness)
          child-witness)))))

;; --- Sequential Conformance & Witnessing ---

(defn conformed-chain-witness
  "Parses a linear notation sequence (syntagma) against a chain grammar.
   Evaluates each token/item in sequence according to slot definitions, cardinalities,
   first-match alternatives (alt-slot), and hierarchical sub-grammars (nested-slot).
   Returns a proof-carrying chain witness:
     {:chain/valid? boolean
      :chain/grammar grammar-id
      :chain/tokens original input vector
      :chain/parsed map of {slot-name conformed-tokens} (when valid)
      :chain/failed-slot slot-name or index where sequence parsing failed (when invalid)
      :chain/unconsumed leftover unparsed tokens}."
  [grammar tokens]
  (let [slots (:grammar/slots grammar)]
    (loop [rem-slots (seq slots)
           rem-tokens (vec tokens)
           parsed {}]
      (if (empty? rem-slots)
        (if (empty? rem-tokens)
          {:chain/valid? true
           :chain/grammar (:grammar/id grammar)
           :chain/tokens tokens
           :chain/parsed parsed
           :chain/failed-slot nil
           :chain/unconsumed []}
          {:chain/valid? false
           :chain/grammar (:grammar/id grammar)
           :chain/tokens tokens
           :chain/parsed parsed
           :chain/failed-slot :excess-tokens
           :chain/unconsumed rem-tokens})
        (let [curr-slot (first rem-slots)
              slot-k (:slot/name curr-slot)
              card (:slot/cardinality curr-slot)]
          (case card
            :one
            (if (empty? rem-tokens)
              {:chain/valid? false
               :chain/grammar (:grammar/id grammar)
               :chain/tokens tokens
               :chain/parsed parsed
               :chain/failed-slot slot-k
               :chain/unconsumed []}
              (let [tok (first rem-tokens)
                    matched (eval-slot-item curr-slot tok)]
                (if (some? matched)
                  (recur (rest rem-slots)
                         (subvec rem-tokens 1)
                         (assoc parsed slot-k matched))
                  {:chain/valid? false
                   :chain/grammar (:grammar/id grammar)
                   :chain/tokens tokens
                   :chain/parsed parsed
                   :chain/failed-slot slot-k
                   :chain/unconsumed rem-tokens})))

            :opt
            (if (empty? rem-tokens)
              (recur (rest rem-slots) rem-tokens parsed)
              (let [tok (first rem-tokens)
                    matched (eval-slot-item curr-slot tok)]
                (if (some? matched)
                  (recur (rest rem-slots)
                         (subvec rem-tokens 1)
                         (assoc parsed slot-k matched))
                  (recur (rest rem-slots)
                         rem-tokens
                         parsed))))

            :star
            (let [matched-run (loop [toks rem-tokens
                                     acc []]
                                (if (empty? toks)
                                  [toks acc]
                                  (let [tok (first toks)
                                        matched (eval-slot-item curr-slot tok)]
                                    (if (some? matched)
                                      (recur (subvec toks 1) (conj acc matched))
                                      [toks acc]))))
                  [unmatched-toks acc] matched-run]
              (recur (rest rem-slots) unmatched-toks (assoc parsed slot-k acc)))

            :plus
            (let [matched-run (loop [toks rem-tokens
                                     acc []]
                                (if (empty? toks)
                                  [toks acc]
                                  (let [tok (first toks)
                                        matched (eval-slot-item curr-slot tok)]
                                    (if (some? matched)
                                      (recur (subvec toks 1) (conj acc matched))
                                      [toks acc]))))
                  [unmatched-toks acc] matched-run]
              (if (empty? acc)
                {:chain/valid? false
                 :chain/grammar (:grammar/id grammar)
                 :chain/tokens tokens
                 :chain/parsed parsed
                 :chain/failed-slot slot-k
                 :chain/unconsumed rem-tokens}
                (recur (rest rem-slots) unmatched-toks (assoc parsed slot-k acc))))))))))

(defn validate-chain
  "Convenience predicate returning true if tokens cleanly satisfy grammar."
  [grammar tokens]
  (:chain/valid? (conformed-chain-witness grammar tokens)))

;; --- Cross-Grammar Commutator Analysis (T-SEM-014) ---

(defn grammar-commutator
  "Compares dual syntagmatic parses Gamma_A(tokens) vs Gamma_B(tokens) across
   the same token sequence to measure structural divergence between competing models.
   Returns a structured grammar commutator record:
     {:commutator/both-valid? boolean
      :commutator/regime :both-match, :grammar-a-only, :grammar-b-only, or :neither-matches
      :commutator/witness-a conformed-chain-witness for grammar-a
      :commutator/witness-b conformed-chain-witness for grammar-b
      :commutator/slot-divergence map comparing parsed slot structures}."
  [grammar-a grammar-b tokens]
  (let [wit-a (conformed-chain-witness grammar-a tokens)
        wit-b (conformed-chain-witness grammar-b tokens)
        val-a? (:chain/valid? wit-a)
        val-b? (:chain/valid? wit-b)
        regime (cond
                 (and val-a? val-b?) :both-match
                 (and val-a? (not val-b?)) :grammar-a-only
                 (and (not val-a?) val-b?) :grammar-b-only
                 :else :neither-matches)]
    {:commutator/both-valid? (and val-a? val-b?)
     :commutator/regime regime
     :commutator/witness-a wit-a
     :commutator/witness-b wit-b
     :commutator/slot-divergence {:grammar-a-slots (vec (keys (:chain/parsed wit-a)))
                                  :grammar-b-slots (vec (keys (:chain/parsed wit-b)))
                                  :shared-slots (set/intersection (set (keys (:chain/parsed wit-a)))
                                                                  (set (keys (:chain/parsed wit-b))))}}))
