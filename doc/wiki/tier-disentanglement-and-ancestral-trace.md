---
uri: seme/wiki/tier-disentanglement-and-ancestral-trace
title: Tier Disentanglement and Retrograde Ancestral Tracing (T-SEM-010)
owner: edphos
status: living
updated: 2026-11-13
description: "Theoretical framework and Clojure implementation for isolating failure tiers across the Mode-D/Mode-S boundary and tracing causal antecedents in derivation chains."
---

# Tier Disentanglement and Retrograde Ancestral Tracing (`T-SEM-010`)

When an empirical encounter fails at a leaf predicate in an axiomatic system, classical Modus Tollens only asserts $\neg(A_1 \wedge \dots \wedge A_n)$. It fails to answer two critical questions:
1. **At which ontological tier did the failure occur?** (Ingress bridge, sovereign base invariant, or contextual selectional overlay?)
2. **Which antecedent step in the derivation chain causally conditioned or produced the failing value?**

`phosphene/seme` formalizes these two mechanisms via `delome/disentangle-provocation-tier` and `pheme/retrograde-ancestor-trace`.

---

## 1. Ontological Tier Disentanglement

We partition leaf failures into three mutually exclusive architectural subsystems (`SEME-013`, `CLJ-002-U`, `TELL-1002`):

```
                                Empirical Leaf Failure
                                          │
        ┌─────────────────────────────────┼─────────────────────────────────┐
        ▼                                 ▼                                 ▼
1. Perimeter Ingress Shock       2. Constitutive Invariant      3. Selectional Restriction
   (:tier/perimeter-ingress-shock)  (:tier/constitutive-invariant)  (:tier/selectional-restriction)
   Raw unqualified coordinate       Sovereign base spec violated   Base spec holds; local
   failed boundary normalization    Target: Sovereign Registry     overlay rule violated
   Target: Ingress Bridge                                          Target: Vertex Overlay
```

### Classification Algorithm:
1. **Perimeter Ingress Shock (`:tier/perimeter-ingress-shock`):**
   The coordinate itself is unqualified (e.g. `:cell-count` without namespace). Indicates that external Mode-S data bypassed or failed the ingress bridge (`delome/rehydrate-unqualified`). Targets the `:ingress-bridge`.
2. **Constitutive Invariant Failure (`:tier/constitutive-invariant`):**
   The value violates the global spec registered under the sovereign two-segment coordinate (`s/get-spec coord`). Targets the `:sovereign-registry`.
3. **Selectional Restriction Failure (`:tier/selectional-restriction`):**
   The value satisfies the global base spec, but violates a contextual overlay rule applied at this specific vertex (`SEME-013`). Targets the `:vertex-overlay`, leaving sovereign base definitions untouched.
4. **Sound (`:tier/sound`):**
   Both the base spec and overlay are satisfied.

---

## 2. Retrograde Derivation Ancestral Tracing

In sequential conformance pipelines (`pheme/derivation-chain-witness`, `CLJ-001-A`):
$$x_0 \xrightarrow{P_0} x_1 \xrightarrow{P_1} \dots \xrightarrow{P_{k-1}} x_k \xrightarrow{P_k} \text{FAIL}$$

When step $P_k$ fails on coordinate path $\pi$:
* The failing value $v = x_k(\pi)$ was not necessarily present in the raw input $x_0$.
* An earlier step $P_j$ ($j < k$) may have calculated, transformed, or enriched $v$.
* `pheme/retrograde-ancestor-trace` scans intermediate step records backwards from step $k-1$ down to 0, identifying the most recent ancestor stage where:
  $$\operatorname{val}(x_{j-1}, \pi) \ne \operatorname{val}(x_j, \pi)$$
* If no intermediate step modified $\pi$, the trace cleanly links the provocation back to the `:initial-referent` at raw ingress.

---

## 3. Clojure Realization

```clojure
(require '[clojure.spec.alpha :as s]
         '[phosphene.seme.delome :as delome]
         '[phosphene.seme.pheme :as pheme])

;; --- 1. Tier Disentanglement ---
(s/def :seme.bio/cell-count pos-int?)
(def vertex-overlay {:seme.bio/cell-count (fn [v] (> v 100))})

;; Case A: Constitutive invariant violated (-5 is not pos-int?)
(delome/disentangle-provocation-tier :seme.bio/cell-count -5 vertex-overlay)
;;=> {:tier/classification :tier/constitutive-invariant, :tier/target-subsystem :sovereign-registry, ...}

;; Case B: Selectional restriction violated (50 is pos-int?, but not > 100)
(delome/disentangle-provocation-tier :seme.bio/cell-count 50 vertex-overlay)
;;=> {:tier/classification :tier/selectional-restriction, :tier/target-subsystem :vertex-overlay, ...}

;; --- 2. Retrograde Ancestor Trace ---
(def p0 (fn [m] (update m :count * 2)))
(def p1 (s/spec (fn [m] (<= (:count m) 15))))

(def witness (pheme/derivation-chain-witness [p0 p1] {:count 10}))
(:witness/valid? witness) ;=> false

(def trace (pheme/retrograde-ancestor-trace witness :count))
(:ancestor/found? trace)             ;=> true
(:ancestor/step-index trace)         ;=> 0
(:ancestor/conditioned-value trace)  ;=> 20
(:ancestor/failing-step-index trace) ;=> 1
```

---

## 4. Invariants & Verification (`s/fdef`)

* **Tier Partition:** Classification is strictly partitioned into `:tier/perimeter-ingress-shock`, `:tier/constitutive-invariant`, `:tier/selectional-restriction`, or `:tier/sound`.
* **Causal Precedence:** Ancestor step index $j$ is strictly prior to failing step index $k$ ($j < k$).
* Verified across 100 randomized trials in `phosphene.seme.core-test` via `clojure.spec.test.alpha`.
