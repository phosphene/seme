---
uri: seme/wiki/boundary-synthesis
title: Automated Differential Falsification and Boundary Specimen Synthesis (CLJ-009, SEME-ARCH-007)
owner: edphos
status: living
updated: 2026-11-13
description: "Theoretical framework and Clojure implementation for automated synthesis of minimal boundary specimens via test.check shrinking in phosphene/seme."
---

# Automated Differential Falsification: Synthesizing Minimal Boundary Specimens (`CLJ-009`, `SEME-ARCH-007`)

In Rich Hickey's `clojure.spec.alpha` (`CLJ-009`, `CLJ-009-S`) and Peirce's Thirdness, specifications are not just passive validators—**a specification is dual to a generator**.

When two competing analytical formalizations or theoretical frameworks ($V_1$ vs. $V_2$) make conflicting truth claims, human researchers often struggle to identify the exact point of divergence. `phosphene.seme.delome/synthesize-boundary-specimen` turns `seme` into an active generative falsifier that automatically synthesizes, stresses, and shrinks specimens to reveal the exact structural chiasm.

---

## 1. Mathematical Formulation

Let $V_1$ and $V_2$ be two analytical vertices or specifications. A **boundary specimen** $x^*$ is defined by the asymmetric boundary condition:
$$x^* \models V_1 \quad \land \quad x^* \not\models V_2$$

### The Synthesis & Shrinking Protocol
1. **Sampling from $V_1$:** Using `clojure.spec.gen.alpha` or vertex keyset generators, generate candidates $x \sim \mathcal{G}(V_1)$.
2. **Boundary Filtering:** Identify candidates satisfying $x \models V_1 \land x \not\models V_2$.
3. **Greedy Structural Shrinking:**
   Given a boundary candidate $x$, systematically minimize its keyset and contents:
   $$x^* = \operatorname{argmin}_{x' \subseteq x} |x'| \quad \text{such that} \quad x' \models V_1 \land x' \not\models V_2$$
   This strips all stochastic noise and incidental attributes, leaving only the bare "bone" of the theoretical conflict.

---

## 2. Clojure Realization in `phosphene.seme.delome`

```clojure
(require '[phosphene.seme.pheme :as pheme]
         '[phosphene.seme.delome :as delome])

;; Define two competing analytical vertices:
;; V1 requires only :phon/ipa
;; V2 requires both :phon/ipa AND :phon/pitch-contour
(def v-broad (pheme/vertex :v/broad [:phon/ipa]))
(def v-narrow (pheme/vertex :v/narrow [:phon/ipa :phon/pitch-contour]))

;; Synthesize the minimal distinguishing boundary specimen
(def result (delome/synthesize-boundary-specimen v-broad v-narrow))

(:found? result)   ;=> true
(:specimen result) ;=> {:phon/ipa "a1b2c3"}
(:satisfies result) ;=> :v/broad
(:falsifies result) ;=> :v/narrow
```

---

## 3. Invariants & Verification (`s/fdef`)

`phosphene.seme.spec` binds formal contracts to both primitives:
* `s/fdef delome/boundary-specimen?`: Pure boolean evaluation of the asymmetric boundary condition.
* `s/fdef delome/synthesize-boundary-specimen`:
  $$\text{ret.found?} = \text{true} \implies \text{boundary-specimen?}(V_1, V_2, \text{ret.specimen}) = \text{true}$$

Verified across 100 randomized trials in `phosphene.seme.core-test` via `clojure.spec.test.alpha`.
