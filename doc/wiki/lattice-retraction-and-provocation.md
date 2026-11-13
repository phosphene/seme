---
uri: seme/wiki/lattice-retraction-and-provocation
title: Poset Lattice Retraction and Axiomatic Provocation (T-SEM-009)
owner: edphos
status: living
updated: 2026-11-13
description: "Theoretical framework and Clojure implementation for returning leaf predicate falsifications as sharp axiomatic provocations via poset lattice retraction."
---

# Poset Lattice Retraction & Structural Provocation (`T-SEM-009`)

In classical Modus Tollens, a leaf failure $\neg L$ only tells us that the conjunction of our theoretical commitments is false:
$$\neg L \implies \neg (A_1 \wedge A_2 \wedge \dots \wedge A_n)$$

It offers no guidance on *which* axiom $A_i$ is at fault. In conventional software systems, this results in uninformative stack traces or arbitrary crash logs. 

In `phosphene/seme`, an analytical model is not a monolithic boolean block; it is an **analytical vertex** embedded in a partially ordered keyset lattice $(V, \le)$:
$$V = \langle \text{req}, \text{opt} \rangle$$

When an empirical encounter fails at one or more required leaf predicates, `phosphene/seme` does not crash or silently mutate the spec. Instead, it computes the **Lattice Retraction** and emits a **Structural Provocation Proof Object**.

---

## 1. Mathematical Formulation

Let $V_{\text{claimed}}$ be an analytical vertex with required coordinates $\mathcal{K}_{\text{req}} = \text{req}(V_{\text{claimed}})$. Let $R$ be an open-world referent map encountered at the system perimeter.

### A. Keyset Partition
We evaluate each coordinate $\kappa \in \mathcal{K}_{\text{req}}$ against $R$:
$$\mathcal{K}_{\text{admitted}} = \{\kappa \in \mathcal{K}_{\text{req}} \mid \kappa \in \operatorname{keys}(R) \wedge R(\kappa) \models \operatorname{Spec}(\kappa)\}$$
$$\mathcal{K}_{\text{falsified}} = \mathcal{K}_{\text{req}} \setminus \mathcal{K}_{\text{admitted}}$$

### B. Lattice Meet Retraction
The **retracted vertex** $V_{\text{retract}}$ represents the maximal sub-model (greatest lower bound) that remains sound under the encounter:
$$V_{\text{retract}} = V_{\text{claimed}} \wedge V_{\text{observed}}$$
$$\text{req}(V_{\text{retract}}) = \mathcal{K}_{\text{admitted}}$$

By construction of the keyset subsumption algebra (`CLJ-002`, `CLJ-002-R/O`):
$$V_{\text{claimed}} \ge V_{\text{retract}} \quad \big(\text{i.e. } \operatorname{subsumes?}(V_{\text{claimed}}, V_{\text{retract}}) = \text{true}\big)$$

### C. Structural Axiomatic Provocation
The plerematic difference isolates the exact structural requirement axioms under pressure:
$$\Delta = \mathcal{K}_{\text{falsified}} = \text{req}(V_{\text{claimed}}) \setminus \mathcal{K}_{\text{admitted}}$$

The failure returns as a formal proof object that provokes the specific axiom that asserted $\Delta$ was an essential requirement of $V_{\text{claimed}}$:
$$\text{Provocation} = \langle V_{\text{claimed}}, V_{\text{retract}}, \Delta_{\text{provoked}}, \mathcal{K}_{\text{preserved}}, \text{Anomalies} \rangle$$

---

## 2. Clojure Realization in `phosphene.seme.pheme`

```clojure
(require '[clojure.spec.alpha :as s]
         '[phosphene.seme.pheme :as pheme])

;; 1. Define sovereign coordinates
(s/def :seme.phenom/luminosity (s/and number? #(<= 0.0 % 1.0)))
(s/def :seme.phenom/thermal-flux (s/and number? pos?))

;; 2. Construct claimed analytical vertex
(def v-flame (pheme/vertex :v/flame-event [:seme.phenom/luminosity :seme.phenom/thermal-flux]))

;; 3. Encounter an anomalous specimen (e.g. cryogenic phosphorescence)
(def anomalous-encounter
  {:seme.phenom/luminosity 0.85
   :seme.phenom/thermal-flux -10.0}) ; Fails pos?

;; 4. Compute Poset Lattice Retraction & Provocation
(def prov (pheme/lattice-provocation v-flame anomalous-encounter))

(:provocation/type prov)
;;=> :structural-requirement-falsification

(:provocation/claimed-vertex prov)
;;=> :v/flame-event

(:provocation/provoked-axioms prov)
;;=> #{:seme.phenom/thermal-flux}

(:provocation/preserved-axioms prov)
;;=> #{:seme.phenom/luminosity}

;; The retracted vertex is sound and subsumed by claimed vertex
(pheme/subsumes? v-flame (:retraction/retracted-vertex (:provocation/retraction prov)))
;;=> true
```

---

## 3. Invariants & Verification (`s/fdef`)

`phosphene.seme.spec` binds formal property invariants to `retract-vertex` and `lattice-provocation`:
* **Subsumption Invariant:** $V_{\text{claimed}} \ge V_{\text{retract}}$ unconditionally.
* **Conservation Invariant:** $\text{req}(V_{\text{claimed}}) = \mathcal{K}_{\text{admitted}} \cup \mathcal{K}_{\text{falsified}}$.
* **Soundness:** Fully satisfied encounters yield `nil` provocation.

Verified across 100 randomized trials in `phosphene.seme.core-test` via `clojure.spec.test.alpha`.
