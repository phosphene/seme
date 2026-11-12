---
uri: seme/wiki/predications
title: Architectural Predications Registry (SEME-ARCH-001 through SEME-ARCH-007)
owner: edphos
status: living
updated: 2026-11-12
description: "Formal architectural predications, logic formulas, and Clojure s-expressions governing phosphene/seme."
---

# Architectural Predications Registry (`SEME-ARCH`)

This document records the foundational architectural predications of `phosphene/seme`. Each entry establishes an invariant coordinate, formal first-order logic specification, and its Clojure implementation rationale.

---

<a id="seme-arch-001"></a>
## SEME-ARCH-001: Sovereign Intension (Hickey's Indirection & Registry Constancy)

* **Coordinate:** `:seme.arch/sovereign-intension`
* **Mode:** `::mode-D`
* **Predicate:** `(anchor :sign/keyword :sense/intensional)`
* **Formal Logic:**
  $$\forall k \Big(\text{SovereignCoord}(k) \implies \big(\text{RegisteredSense}(k) \land \text{ContainerIndependent}(k)\big)\Big)$$
* **Clojure S-Expression:**
  ```clojure
  (s/def :seme.feat/telic boolean?)
  (s/def :seme.ont/animate #{:human :animal})
  ```
* **Definition & Grounding:**
  Leverages Rich Hickey's core insight (`CLJ-014`, `TELL-1001`) that binding predicates to globally namespaced keywords decouples attribute meaning from host record types. In traditional nominal hierarchies, a field named `id` or `type` is subordinate to its enclosing class (`User.id`, `Token.id`). In `seme`, attributes are sovereign intensional units that carry identical verification contracts regardless of which open-world map carries them.

---

<a id="seme-arch-002"></a>
## SEME-ARCH-002: Open-Vertex Projection (Hickey's Open-World Keyset Membership)

* **Coordinate:** `:seme.arch/open-vertex-projection`
* **Mode:** `::mode-D`
* **Predicate:** `(project :referent/map :vertex/keyset)`
* **Formal Logic:**
  $$\forall m \, \forall V \Big(\text{AdmitsVertex}(m, V) \iff \big(\text{IsMap}(m) \land \text{Keys}(m) \supseteq \text{ReqKeys}(V)\big)\Big)$$
* **Clojure S-Expression:**
  ```clojure
  (s/def :seme.vertex/phonological
    (s/keys :req [:seme.ling/id :seme.phon/ipa]))

  (s/def :seme.vertex/morphosyntactic
    (s/keys :req [:seme.ling/id :seme.ling/morpheme-class]))
  ```
* **Definition & Grounding:**
  Employs `s/keys` (`CLJ-002`) and the Open-World Assumption (`CLJ-004`) to project multiple orthogonal analytical vertices onto a single shared referent map. A single linguistic specimen (e.g. `{:seme.ling/id "w1" :seme.phon/ipa "/kæt/" :seme.syn/pos :noun :extra/dialect :scots}`) is simultaneously admitted by phonology, syntax, and dialectology vertices without nominal class collision or key erasure.

---

<a id="seme-arch-003"></a>
## SEME-ARCH-003: Proof-Carrying Parse (Hickey's Conformance & Symbolic Form Capture)

* **Coordinate:** `:seme.arch/proof-carrying-parse`
* **Mode:** `::mode-D`
* **Predicate:** `(reify :truth/witness :ast/conformed)`
* **Formal Logic:**
  $$\forall P \, \forall x \Big(\text{conform}(P, x) \ne \text{:clojure.spec.alpha/invalid} \implies \big(\text{WitnessAST}(\text{conform}(P, x)) \land \text{SymbolicForm}(\text{s/form}(P))\big)\Big)$$
* **Clojure S-Expression:**
  ```clojure
  (defn conformed-witness [spec-k referent]
    (let [valid? (s/valid? spec-k referent)]
      {:witness/spec spec-k
       :witness/valid? valid?
       :witness/parsed (when valid? (s/conform spec-k referent))
       :witness/anatomy (s/form spec-k)
       :witness/anomaly (when-not valid? (s/explain-data spec-k referent))
       :witness/referent referent}))
  ```
* **Definition & Grounding:**
  Eliminates boolean blindness in computational ontology. Rather than yielding an opaque `false` or throwing an exception, verification reifies a *proof-carrying witness* containing the structured conformed parse tree, the exact symbolic `s/form` that evaluated it, and the pristine unmutated referent.

---

<a id="seme-arch-004"></a>
## SEME-ARCH-004: Homoiconic Componential Algebra (Data-First Schema Manipulation)

* **Coordinate:** `:seme.arch/homoiconic-algebra`
* **Mode:** `::mode-D`
* **Predicate:** `(rewrite :predicate/ast :morphism/walk)`
* **Formal Logic:**
  $$\forall S \, \forall f \Big(\text{ComponentialSchema}(S) \implies \text{IsEDN}(S) \land \text{Walkable}(S, f)\Big)$$
* **Clojure S-Expression:**
  ```clojure
  (let [verb-semes (rheme/sememe [s-telic s-animate])
        noun-semes (rheme/sememe [s-animate s-concrete])]
    (rheme/plerematic-difference verb-semes noun-semes))
  ```
* **Definition & Grounding:**
  Treats distinctive semantic features and predicate bundles as first-class immutable data (EDN sets and maps). Predicative features can be merged, subtracted, intersected, and diffed using pure relational algebra without macro-expansion or opaque runtime class generation.

---

<a id="seme-arch-005"></a>
## SEME-ARCH-005: Scoped Vertex Overlays (Contextual Polysemy & Selectional Restrictions)

* **Coordinate:** `:seme.arch/scoped-vertex-overlay`
* **Mode:** `::mode-D`
* **Predicate:** `(layer :registry/base :registry/vertex)`
* **Formal Logic:**
  $$\forall V_1 \, \forall V_2 \, \forall k \Big(V_1 \ne V_2 \implies \text{Eval}\big(k, \text{Composite}(R_{\text{base}}, R_{V1})\big) \ne \text{Eval}\big(k, \text{Composite}(R_{\text{base}}, R_{V2})\big)\Big)$$
* **Clojure S-Expression:**
  ```clojure
  (let [base-reg {:ling/valence vector?}
        intransitive-overlay {:ling/valence (fn [v] (= 1 (count v)))}
        ditransitive-overlay {:ling/valence (fn [v] (= 3 (count v)))}]
    (delome/evaluate-overlay (delome/contextual-overlay base-reg intransitive-overlay) :ling/valence [:subj]))
  ```
* **Definition & Grounding:**
  Resolves the apparent tension between global intensional constancy (`TELL-1001`) and context-dependent linguistic selectional restrictions. By layering vertex-local contextual overlays over a sovereign base registry, individual analytical perspectives specialize local truth conditions without mutating or clobbering the global coordinate.

---

<a id="seme-arch-006"></a>
## SEME-ARCH-006: Bifunctorial Bridge (Lowering Spec.Alpha & Lifting Empirical Corpora)

* **Coordinate:** `:seme.arch/bifunctorial-bridge`
* **Mode:** `::mode-D/S`
* **Predicate:** `(iso :spec/intensional :malli/homoiconic)`
* **Formal Logic:**
  $$\Big(\forall S \, \big(\text{Spec}(S) \implies \exists M \, (\text{Lower}(\text{s/form}(S)) = M)\big)\Big) \land \Big(\forall D \, \big(\text{Corpus}(D) \implies \exists S \, (\text{Lift}(\text{Infer}(D)) = S)\big)\Big)$$
* **Clojure S-Expression:**
  ```clojure
  (let [empirical-corpus [{:id "1" :count 10 :label "alpha"}
                          {:id "2" :count 20 :label "beta"}
                          {:id "3" :count 30 :extra true}]]
    (delome/infer-candidate-spec empirical-corpus))
  ```
* **Definition & Grounding:**
  Establishes a bidirectional bridge across the deductive/empirical boundary (`:mode-D` $\leftrightarrow$ `:mode-S`). Top-down deductive invariants reified in `s/form` lower into inspectable symbolic vectors, while bottom-up empirical observation vectors inductively distill candidate `s/def` required keys and primitive type habits.

---

<a id="seme-arch-007"></a>
## SEME-ARCH-007: Differential Falsification (Cross-Vertex Boundary Stress & Shrinking)

* **Coordinate:** `:seme.arch/differential-falsification`
* **Mode:** `::mode-D`
* **Predicate:** `(contrast :vertex/v1 :vertex/v2 :gen/shrink)`
* **Formal Logic:**
  $$\forall V_1 \, \forall V_2 \Big(\exists x \, \big(\text{Valid}(V_1, x) \land \lnot \text{Valid}(V_2, x)\big) \implies \text{Minimizes}\big(\text{shrink}, x, \text{BoundarySpecimen}(V_1, V_2)\big)\Big)$$
* **Clojure S-Expression:**
  ```clojure
  (delome/boundary-specimen? :vertex/broad-phonology :vertex/narrow-phonetics specimen)
  ```
* **Definition & Grounding:**
  Combines `clojure.spec.gen.alpha` with `test.check` shrinking to synthesize minimal boundary specimens—referents that satisfy one analytical vertex while falsifying another. This systematically exposes the exact structural chiasms and boundary conditions separating competing theoretical formalizations.
