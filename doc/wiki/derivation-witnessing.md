---
uri: seme/wiki/derivation-witnessing
title: Derivation-Chain Witnessing and Conjunction Tracing (CLJ-001-A, CLJ-006, SEME-ARCH-003)
owner: edphos
status: living
updated: 2026-11-13
description: "Theoretical framework and Clojure implementation for auditing step-by-step re-hydration in s/and conjunction pipelines in phosphene/seme."
---

# Derivation-Chain Witnessing & Conjunction Tracing (`CLJ-001-A`, `CLJ-006`, `SEME-ARCH-003`)

In Rich Hickey's `clojure.spec.alpha` (`CLJ-001-A`), `s/and` is not merely short-circuiting boolean conjunction. It is a **sequential pipeline** where each subsequent predicate receives the conformed output of the preceding stage:
$$(s/\text{and} \; p_1 \; p_2 \; \dots \; p_n) \implies \text{Valid}(x) \iff \big(p_1(x) \land p_2(p_1\text{-conform}(x)) \dots \big)$$

In formal scientific computing, epistemics, and philological verification, knowing that a pipeline succeeded or failed is insufficient. We require an immutable, proof-carrying record of the **derivation history**: how raw empirical tokens were progressively parsed, cleaned, coerced, and validated into typed ontological entities.

`phosphene.seme.pheme/derivation-chain-witness` reifies this derivation trace into an auditable proof object.

---

## 1. Mathematical Formulation

Let $\langle P_1, P_2, \dots, P_n \rangle$ be an ordered sequence of predicates or conformance gates. Given an initial empirical referent $x_0$:

$$\begin{aligned}
x_1 &= \operatorname{conform}(P_1, x_0) \\
x_2 &= \operatorname{conform}(P_2, x_1) \\
&\;\;\vdots \\
x_k &= \operatorname{conform}(P_k, x_{k-1})
\end{aligned}$$

* **Success Condition:**
  $$\forall i \in \{1, \dots, n\}, \; x_i \ne \text{:clojure.spec.alpha/invalid} \implies \text{Valid} \land x_{\text{final}} = x_n$$
* **Failure Localization:**
  $$\exists k \in \{1, \dots, n\} \text{ s.t. } x_k = \text{::invalid} \implies \text{FailedAtStep}(k) \land \text{Anomaly} = \operatorname{explain-data}(P_k, x_{k-1})$$

The derivation trace preserves the unmutated input $x_0$, all intermediate conformed representations $\langle x_1, \dots, x_k \rangle$, and the exact failure point if invalid.

---

## 2. Clojure Realization in `phosphene.seme.pheme`

```clojure
(require '[clojure.spec.alpha :as s]
         '[phosphene.seme.pheme :as pheme])

;; Define a multi-stage re-hydration pipeline
(s/def :pipeline/digit-str (s/and string? #(re-matches #"\d+" %)))
(s/def :pipeline/parsed-int (s/conformer #(Integer/parseInt %)))
(s/def :pipeline/positive pos?)

(def pipeline [:pipeline/digit-str :pipeline/parsed-int :pipeline/positive])

;; 1. Successful Derivation Trace
(def witness (pheme/derivation-chain-witness pipeline "42"))

(:witness/valid? witness) ;=> true
(:witness/initial witness) ;=> "42"
(:witness/final witness)   ;=> 42
(mapv :step/output (:witness/steps witness))
;;=> ["42" 42 42]

;; 2. Failed Derivation Trace with Failure Localization
(def failed-witness (pheme/derivation-chain-witness pipeline "-10"))

(:witness/valid? failed-witness)       ;=> false
(:witness/failed-step failed-witness)  ;=> 0 (failed at string digits check)
(get-in failed-witness [:witness/steps 0 :step/anomaly])
;;=> {:clojure.spec.alpha/problems [...]}
```

---

## 3. Invariants & Verification (`s/fdef`)

`phosphene.seme.spec` binds formal contracts to derivation chain witnessing:
* Initial referent conservation:
  $$\text{ret.witness/initial} = \text{referent}$$
* Step count boundary:
  $$|\text{ret.witness/steps}| \le |\text{pipeline}|$$
  Execution terminates immediately upon the first failing step without executing downstream stages.

Verified across 100 randomized trials in `phosphene.seme.core-test` via `clojure.spec.test.alpha`.
