(ns phosphene.seme.chain
  "Sequential Predicative Grammars & Syntagma Interoperability (CLJ-007, CLJ-007-C/A/R).
   Composes horizontal syntagmatic chaining (linear order sigma_1 prec sigma_2 prec sigma_3)
   with internal distinctive semantic features (semes and sememes).
   Parses notation vectors against regex grammars where sequence positions are constrained
   by unsaturated feature predicates."
  (:require [clojure.spec.alpha :as s]
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
    :slot/pred (if (rheme/seme? pred-or-seme)
                 (:seme/pred pred-or-seme)
                 pred-or-seme)
    :slot/cardinality cardinality}))

(defn slot?
  "Returns true if x is a valid grammar slot."
  [x]
  (and (map? x)
       (keyword? (:slot/name x))
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
   Returns a grammar record containing the slot definitions and a compiled s/cat regex form."
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

;; --- Sequential Conformance & Witnessing ---

(defn match-slot?
  "Tests whether a token or referent item satisfies a slot's predicate."
  [slot-def item]
  (let [p (:slot/pred slot-def)]
    (cond
      (keyword? p) (s/valid? p item)
      (fn? p) (boolean (p item))
      :else false)))

(defn conformed-chain-witness
  "Parses a linear notation sequence (syntagma) against a chain grammar.
   Evaluates each token/item in sequence according to slot definitions and cardinalities.
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
              (let [tok (first rem-tokens)]
                (if (match-slot? curr-slot tok)
                  (recur (rest rem-slots)
                         (subvec rem-tokens 1)
                         (assoc parsed slot-k tok))
                  {:chain/valid? false
                   :chain/grammar (:grammar/id grammar)
                   :chain/tokens tokens
                   :chain/parsed parsed
                   :chain/failed-slot slot-k
                   :chain/unconsumed rem-tokens})))

            :opt
            (if (empty? rem-tokens)
              (recur (rest rem-slots) rem-tokens parsed)
              (let [tok (first rem-tokens)]
                (if (match-slot? curr-slot tok)
                  (recur (rest rem-slots)
                         (subvec rem-tokens 1)
                         (assoc parsed slot-k tok))
                  (recur (rest rem-slots)
                         rem-tokens
                         parsed))))

            :star
            (let [matching (vec (take-while #(match-slot? curr-slot %) rem-tokens))
                  n-matched (count matching)]
              (recur (rest rem-slots)
                     (subvec rem-tokens n-matched)
                     (assoc parsed slot-k matching)))

            :plus
            (let [matching (vec (take-while #(match-slot? curr-slot %) rem-tokens))
                  n-matched (count matching)]
              (if (zero? n-matched)
                {:chain/valid? false
                 :chain/grammar (:grammar/id grammar)
                 :chain/tokens tokens
                 :chain/parsed parsed
                 :chain/failed-slot slot-k
                 :chain/unconsumed rem-tokens}
                (recur (rest rem-slots)
                       (subvec rem-tokens n-matched)
                       (assoc parsed slot-k matching))))))))))

(defn validate-chain
  "Convenience predicate returning true if tokens cleanly satisfy grammar."
  [grammar tokens]
  (:chain/valid? (conformed-chain-witness grammar tokens)))
