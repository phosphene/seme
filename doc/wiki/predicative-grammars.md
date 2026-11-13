---
uri: seme/wiki/predicative-grammars
title: Sequential Predicative Grammars & Syntagmatic Interoperability (CLJ-007, CLJ-007-C/A/R)
owner: edphos
status: living
updated: 2026-11-13
description: "Theoretical framework and Clojure implementation for parsing linear sequence chains against regex grammars constrained by unsaturated distinctive feature predicates."
---

# Sequential Predicative Grammars & Syntagmatic Interoperability (`CLJ-007`, `CLJ-007-C/A/R`)

In computational linguistics and formal semiotics, syntax and semantics are frequently complected into monolithic trees. In the Phosphene platform architecture, we de-complect this along two orthogonal dimensions:

1. **Horizontal Syntagmatic Ordering (`phosphene.syntagma`):**
   Governs linear precedence relations ($\sigma_1 \prec \sigma_2 \prec \sigma_3$), transitional probabilities, and poset ordering over notation tokens.
2. **Vertical / Internal Distinctive Features (`phosphene.seme`):**
   Governs componential feature bundles (semes and sememes), analytical vertices, and perceptual admission.

Rich Hickey's formulation of sequential specifications (`CLJ-007`, `CLJ-007-C/A/R`) recognizes that sequences are fundamentally regular expressions over predicates (`s/cat`, `s/alt`, `s/*`, `s/+`, `s/?`). In `phosphene.seme.chain`, we reify this into **Sequential Predicative Grammars**, allowing syntagmatic sequence chains to be validated and conformed where each slot in the regular grammar is constrained by an unsaturated feature predicate or analytical vertex.

---

## 1. Mathematical Formulation

Let a sequence $S = \langle t_1, t_2, \dots, t_n \rangle$ be an ordered syntagmatic chain of referent tokens.

A **Predicative Grammar** $\Gamma$ is an ordered sequence of slots:
$$\Gamma = \langle \text{Slot}_1, \text{Slot}_2, \dots, \text{Slot}_m \rangle$$

Where each slot is a tuple:
$$\text{Slot}_k = \langle \kappa_k, P_k, c_k \rangle$$
- $\kappa_k \in \mathcal{K}_{\text{slot}}$: Unique semantic role label (e.g., `:subj`, `:verb`, `:obj`).
- $P_k$: An unsaturated seme predicate or coordinate spec ($P_k: \mathcal{D} \to \{0, 1\}$).
- $c_k \in \{ \mathbf{1}, \mathbf{?}, \mathbf{*}, \mathbf{+} \}$: Cardinality constraint.

### Conformed Chain Witness
Parsing sequence $S$ against $\Gamma$ yields a proof-carrying witness record $W$:
$$W = \operatorname{conformed-chain-witness}(\Gamma, S)$$
- When valid:
  $$\forall k, \quad \operatorname{tokens}(\kappa_k) \models P_k \quad \land \quad \operatorname{count}(\kappa_k) \models c_k$$
  Yielding an unmutated AST mapping each slot label to its matched conformed token(s).
- When invalid:
  Identifies the exact failing slot label $\kappa_{\text{failed}}$ and isolates the unconsumed suffix tokens without stateful mutation.

---

## 2. Clojure Realization in `phosphene.seme.chain`

```clojure
(require '[phosphene.seme.chain :as chain])

;; 1. Construct Slots with Unsaturated Semes or Predicates
(def s-subj (chain/slot :subj (fn [x] (= (:pos x) :noun))))
(def s-verb (chain/slot :verb (fn [x] (= (:pos x) :verb))))
(def s-obj  (chain/slot :obj  (fn [x] (= (:pos x) :noun)) :opt))

;; 2. Construct Sequential Chain Grammar (e.g. SVO grammar)
(def svo-grammar
  (chain/chain-grammar :grammar/svo [s-subj s-verb s-obj]))

;; 3. Parse Syntagmatic Sequence of Feature Bundles
(def tokens
  [{:word "dog"  :pos :noun :anim true}
   {:word "chases" :pos :verb :telic true}
   {:word "ball" :pos :noun :anim false}])

(def witness (chain/conformed-chain-witness svo-grammar tokens))

(:chain/valid? witness)
;;=> true

(:chain/parsed witness)
;;=> {:subj {:word "dog", :pos :noun, :anim true},
;;    :verb {:word "chases", :pos :verb, :telic true},
;;    :obj  {:word "ball", :pos :noun, :anim false}}

;; 4. Sequence Violation Isolation
(def invalid-tokens
  [{:word "chases" :pos :verb}
   {:word "dog"    :pos :noun}])

(def fail-witness (chain/conformed-chain-witness svo-grammar invalid-tokens))
(:chain/valid? fail-witness)
;;=> false
(:chain/failed-slot fail-witness)
;;=> :subj
(:chain/unconsumed fail-witness)
;;=> [{:word "chases", :pos :verb} {:word "dog", :pos :noun}]
```

---

## 3. Invariants & Verification (`s/fdef`)

`phosphene.seme.spec` binds formal property contracts to `chain/chain-grammar` and `chain/conformed-chain-witness`:
* Grammar ID and slots are preserved.
* Output witness always preserves the unmutated input token vector.
* Verified across 100 randomized trials in `phosphene.seme.core-test` via `clojure.spec.test.alpha`.
