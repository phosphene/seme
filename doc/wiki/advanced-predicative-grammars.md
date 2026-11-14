---
uri: seme/wiki/advanced-predicative-grammars
title: Advanced Predicative Regex Grammars: Alt-Slots, Hierarchical Nesting, and Grammar Commutators (T-SEM-014)
owner: edphos
status: living
updated: 2026-11-14
description: "Architecture and formal specification for labeled branch alternatives, scoped hierarchical sub-grammar nesting, and cross-grammar commutator analysis grounded in Hickey habits."
---

# Advanced Predicative Regex Grammars (`T-SEM-014`)

> *"Sequence specs use a set of standard regular expression operators... cat, alt, *, +, ?, &... Note that cat and alt require all of their components be labeled, and the return value of each is a map with the keys corresponding to the matched components."*  
> — Rich Hickey, `clj_spec_master_v4.1`

`phosphene.seme.chain` composes horizontal syntagmatic chaining ($\sigma_1 \prec \sigma_2 \prec \sigma_3$) with internal distinctive sememic feature bundles. 

`T-SEM-014` builds upon our genealogical ethos: **anchoring parser evaluation in Hickey's habits at the basis**—specifically deterministic, ordered first-match evaluation without non-deterministic backtracking—while delivering clean, layered refinements for **labeled branch alternatives (`alt-slot`)**, **scoped hierarchical sub-grammar nesting (`nested-slot`)**, and **cross-grammar commutator analysis (`grammar-commutator`)**.

---

## 1. The Genealogical Ethos: First-Match Priority vs. Backtracking

In formal language theory, context-free parsing often relies on backtracking loops or generalized LR/GLR parsers. However, in `clojure.spec.alpha`, Rich Hickey committed to **deterministic derivative parsing**:
1. **Predictability:** Parser execution time is an unambiguous linear function of token stream length. There are no surprise exponential backtracking explosions.
2. **First-Match as Semantic Priority:** In scientific discourse and empirical taxonomy, ordering branches in an alternative slot expresses **explicit epistemic precedence**:
   ```clojure
   (chain/alt-slot :evidence
     [[:exact-citation (s/and string? #(str/starts-with? % ">"))]
      [:code-snippet   (s/and string? #(str/starts-with? % "`"))]
      [:general-note   string?]]
     :plus)
   ```
   If a token satisfies `:exact-citation`, it is conformed under that branch immediately. The more specific, falsifiable claim takes precedence over the looser fallback.

---

## 2. Delivered Primitives (`phosphene.seme.chain`)

### A. Labeled Alternative Slots (`chain/alt-slot`, `CLJ-007-ALT`)
- **Constructor:** `(chain/alt-slot name-k branches [cardinality])`
- Evaluates branches in declared order.
- **Proof-Carrying Conformance:** Rather than boolean blindness, the conformed parse tags which branch was satisfied:
  ```clojure
  ;; Conformed result in witness:
  {:evidence [[:exact-citation "> Intact membrane required"]]}
  ```

### B. Scoped Hierarchical Sub-Grammars (`chain/nested-slot`, `CLJ-007-NEST`)
- **Constructor:** `(chain/nested-slot name-k child-grammar [cardinality])`
- **Escapes the Flatness Trap:** In `clojure.spec.alpha`, nesting an inner `(s/cat ...)` inside an outer `cat` flattens the token stream, requiring the cryptic `(s/spec ...)` wrapper to halt absorption.
- `nested-slot` natively matches a nested sequence/vector against an autonomous `child-grammar`.
- Embeds the full proof-carrying child witness (`conformed-chain-witness`) directly in the parent AST, with complete slot bindings and failure localization.

### C. Cross-Grammar Commutator Analysis (`chain/grammar-commutator`)
- **Signature:** `(chain/grammar-commutator grammar-a grammar-b tokens)`
- Directly extends our Bohrian complementarity architecture (`T-SEM-013`): compares two competing analytical grammars ($\Gamma_A$ and $\Gamma_B$) across the exact same sequential text.
- Returns a structured commutator record:
  - `:commutator/both-valid?`: true iff the text satisfies both discourse grammars.
  - `:commutator/regime`: `:both-match`, `:grammar-a-only`, `:grammar-b-only`, or `:neither-matches`.
  - `:commutator/witness-a` & `:commutator/witness-b`: complete parsed ASTs.
  - `:commutator/slot-divergence`: isolates shared vs unique structural slots.

---

## 3. Concrete Example: Mythic Narrative with Nested Ordeals

```clojure
(ns example.metamyth
  (:require [phosphene.seme.core :as seme]))

;; 1. Define Inner Ordeal Sub-Grammar
(def ordeal-grammar
  (seme/chain-grammar
    :grammar/underworld-ordeal
    [(seme/slot :trial keyword?)
     (seme/slot :response keyword?)]))

;; 2. Define Hero Journey with Nested Slot and Alt-Slot
(def hero-myth-grammar
  (seme/chain-grammar
    :grammar/hero-journey
    [(seme/slot :departure string?)
     (seme/nested-slot :ordeal ordeal-grammar :one)
     (seme/alt-slot :boon
                    [[:sacred-seed (s/and string? #(re-find #"seed|corn" %))]
                     [:celestial-tool keyword?]]
                    :one)
     (seme/slot :return string?)]))

;; 3. Parse Token Sequence
(def tokens
  ["Departure from Highland Milpa"
   [:trial/dark-house :response/cigar-glow]
   "Gift of yellow corn seeds"
   "Return to the surface world"])

(def witness (seme/conformed-chain-witness hero-myth-grammar tokens))

(:chain/valid? witness)
;;=> true

(get-in witness [:chain/parsed :ordeal :chain/parsed])
;;=> {:trial :trial/dark-house, :response :response/cigar-glow}

(get-in witness [:chain/parsed :boon])
;;=> [:sacred-seed "Gift of yellow corn seeds"]
```
