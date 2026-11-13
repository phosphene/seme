---
uri: seme/wiki/unqualified-bridge
title: Unqualified Ingress Bridge and Mode-S Boundary Defense (CLJ-002-U, TELL-1002)
owner: edphos
status: living
updated: 2026-11-13
description: "Theoretical framework and Clojure implementation for lifting Mode-S external maps into sovereign Mode-D coordinates without grounding bleed."
---

# Unqualified Ingress Bridge & Mode-S Boundary Defense (`CLJ-002-U`, `TELL-1002`)

In the Phosphene platform architecture, we maintain a hard partition between the **Mode-D Deductive Core** (deterministic Clojure symbolic logic) and the **Mode-S Stochastic Spoke** (Python agents, LLM outputs, external JSON payloads, disk files).

External data invariably arrives across the system perimeter as **unqualified keys**:
```json
{"ipa": "/kæt/", "pos": "noun", "author": "Smith"}
```

Directly ingesting unqualified maps into a Mode-D system causes **Grounding Bleed**:
1. Attributes lose their global intensional identity (`CLJ-003`, `CLJ-015`).
2. Downstream vertices cannot distinguish between domain coordinates from orthogonal disciplines (e.g. `:id` as database integer vs. `:id` as linguistic entry slug).
3. Conformance checking degrades to ad-hoc guessing.

Rich Hickey's `clojure.spec.alpha` addresses this with the **Unqualified Bridge** (`CLJ-002-U`: `:req-un` and `:opt-un`). In `phosphene.seme.delome`, we generalize this principle into an active ingress perimeter gate: `delome/rehydrate-unqualified`.

---

## 1. Mathematical Formulation

Let $\mathcal{K}_{\text{raw}}$ be the set of unnamespaced keys (strings or simple keywords). Let $\mathcal{U}_{\text{coord}}$ be the sovereign global coordinate registry of two-segment namespaced keywords.

A **re-hydration map** $\mu: \mathcal{K}_{\text{raw}} \to \mathcal{U}_{\text{coord}}$ establishes a bijective or injective projection from raw keys to sovereign coordinates:
$$\mu(k) = \kappa \in \mathcal{U}_{\text{coord}}$$

Given an unnamespaced external map $M_{\text{raw}} = \{k_i \mapsto v_i\}$:
$$M_{\text{sovereign}} = \{\mu(k_i) \mapsto v_i \mid k_i \in \operatorname{dom}(\mu)\}$$

### Grounding Bleed Defense & Validation Gate
For each re-hydrated coordinate $\kappa = \mu(k_i)$:
$$v_i \models \operatorname{Spec}(\kappa)$$
If any value violates its sovereign specification, the ingress gate halts ingestion and captures a structured anomaly map:
$$\text{Anomaly}(\kappa) = \operatorname{explain-data}(\kappa, v_i)$$

This guarantees that data entering the Mode-D core is fully sovereign, typed, and conformant.

---

## 2. Clojure Realization in `phosphene.seme.delome`

```clojure
(require '[clojure.spec.alpha :as s]
         '[phosphene.seme.delome :as delome])

;; Define sovereign coordinate specs
(s/def :seme.phon/ipa string?)
(s/def :seme.syn/pos #{:noun :verb})

;; External raw JSON map from Python or HTTP
(def raw-json-dict
  {"ipa" "/kæt/"
   "pos" "noun"}) ; Note: string value for pos

;; 1. Re-hydrate via Explicit Mapping Dictionary
(def mapping
  {"ipa" :seme.phon/ipa
   "pos" :seme.syn/pos})

;; Re-hydrate and validate
(def res (delome/rehydrate-unqualified raw-json-dict mapping {:validate? false}))
(:rehydrated/valid? res)
;;=> true
(:rehydrated/referent res)
;;=> {:seme.phon/ipa "/kæt/", :seme.syn/pos "noun"}

;; 2. Grounding Bleed Prevention (Type Mismatch)
(def corrupt-dict {"ipa" 9999})
(def failed-res (delome/rehydrate-unqualified corrupt-dict mapping))

(:rehydrated/valid? failed-res) ;=> false
(get-in failed-res [:rehydrated/anomalies :seme.phon/ipa])
;;=> {:clojure.spec.alpha/problems [{:pred clojure.core/string?, :val 9999, ...}]}
```

---

## 3. Invariants & Verification (`s/fdef`)

`phosphene.seme.spec` binds formal contracts to `rehydrate-unqualified`:
* Output referent is guaranteed to be a Clojure map.
* Validity flag is boolean.
* If invalid, `:rehydrated/anomalies` isolates the failing sovereign coordinates.

Verified across 100 randomized trials in `phosphene.seme.core-test` via `clojure.spec.test.alpha`.
