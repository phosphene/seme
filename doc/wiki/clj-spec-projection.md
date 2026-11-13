---
uri: seme/wiki/clj-spec-projection
title: Projection of clojure.spec.alpha Master Coordinates (CLJ-*) in phosphene/seme
owner: edphos
status: living
updated: 2026-11-13
description: "Hermetic projection and formal mapping of clojure.spec.alpha core coordinates (CLJ-*, TELL-*) into the predication architecture of phosphene/seme."
---

# Projection of `clojure.spec.alpha` Master Coordinates (`CLJ-*`) in `phosphene/seme`

> *"It is better to have 100 functions operate on one data structure than 10 functions on 10 data structures."*  
> — Alan Perlis
>
> *"We de-complect the Sign from the Referent via the Namespaced Sense."*  
> — `clj_spec_master_v4.1`

`phosphene/seme` is designed as a meta-predicative apparatus: a library that studies **predication itself** as an inspectable, transformable, and multi-perspectival object of formal study. 

This document establishes the formal projection of the canonical `clojure.spec.alpha` master coordinates (`CLJ-*`, `TELL-*`) into `seme`, articulating how Hickey’s intensional architecture reifies each operation and how it grounds our Peircean triadic modules (`rheme`, `pheme`, `delome`).

---

## 1. Master Coordinate Projection Table

| Master Coordinate | ID | Predicate / Invariant | Realization in `phosphene/seme` | Peircean & Architectural Role |
| :--- | :--- | :--- | :--- | :--- |
| [`:clj.spec.op/predication`](#clj-001) | **CLJ-001** | `(is :spec/logic :math.logic/composition)` | Pure Clojure boolean functions reified as unsaturated feature records (`rheme/seme`). | **Firstness**: Elementary qualitative affordance prior to propositional assertion. |
| [`:clj.spec.op/conjunction`](#clj-001-a) | **CLJ-001-A** | `(compose :spec/and :logic/conjunction)` | Sequential pipeline where conformed output flows into subsequent predicates. | Re-hydration chaining across feature filters. |
| [`:clj.spec.op/disjunction`](#clj-001-o) | **CLJ-001-O** | `(compose :spec/or :logic/disjunction)` | Tagged disjunction providing labeled branch paths for deterministic destructuring. | Deterministic selection across competing analytical interpretations. |
| [`:clj.spec.map/membership`](#clj-002) | **CLJ-002** | `(is :spec/maps :math.logic/membership)` | `s/keys` as pure keyset presence checks; attribute senses defined globally. | Decouples attribute meaning from host record types. |
| [`:clj.spec.map/req-keys`](#clj-002-r) | **CLJ-002-R** | `(enforce :map/required-keys)` | Sovereign coordinate keyset gating in `pheme/admits-vertex?`. | Structural admission gate for analytical vertices. |
| [`:clj.spec.map/unqualified`](#clj-002-u) | **CLJ-002-U** | `(map :req-un :opt-un)` | Mode-S bridge mapping unqualified JSON/external maps to sovereign coordinates. | Boundary defense against grounding bleed at ingest perimeter. |
| [`:clj.spec.sys/global-registry`](#clj-003) | **CLJ-003** | `(bind :math.prop/identity :math.prop/coordinate)` | Sovereign namespaced keywords (`:seme.feat/*`, `:seme.ont/*`) in global `s/def`. | Timeless intensional identity independent of local record scope. |
| [`:clj.spec.sys/open-world`](#clj-004) | **CLJ-004** | `(allow :data/accretion :logic/ignored)` | Open-world maps carry multi-vertex features without key stripping or collision. | Foundation for simultaneous multi-vertex co-projection (`SEME-ARCH-002`). |
| [`:clj.spec.sys/conformance-gate`](#clj-006) | **CLJ-006** | `(transform :data/referent :spec/sense)` | `pheme/conformed-witness` transforming raw referents into proof-carrying witness ASTs. | **Secondness**: Reification of perceptual judgment without boolean blindness. |
| [`:clj.spec.sys/conform-invalid`](#clj-006-i) | **CLJ-006-I** | `(detect :conform/invalid :logic/fail)` | `pheme/explain-anomaly` extracting structured machine diagnostics from failures. | Formal boundary detection and error localization. |
| [`:clj.spec.seq/regex-grammar`](#clj-007) | **CLJ-007** | `(parse :spec/sequences :math.logic/regex)` | Sequential notation parsed via `s/cat`, `s/alt`, `s/*`, `s/+`. | Composes with `phosphene/syntagma` for linear syntagmatic chain parsing. |
| [`:clj.spec.gen/leverage`](#clj-009) | **CLJ-009** | `(derive :spec/sense :data/reality)` | Duality of intensional spec and test generator in `phosphene.seme.spec`. | Generates reality from specification for automated stress verification. |
| [`:clj.spec.test/property-check`](#clj-009-c) | **CLJ-009-C** | `(check :fn/invariant :test.check/property)` | `stest/check` running $\ge 100$ randomized generative trials per symbol. | Falsification engine proving mathematical invariants. |
| [`:clj.spec.test/shrinking`](#clj-009-s) | **CLJ-009-S** | `(minimize :test.check/failure :lab.sys/root-cause)` | `test.check` shrinking to isolate minimal boundary specimens (`delome/boundary-specimen?`). | Strips stochastic noise to reveal the bare "bone" of an invariant violation. |
| [`:clj.spec.fn/fdef`](#clj-012) | **CLJ-012** | `(fix :fn/contract :spec/triad)` | Triadic contracts (`:args`, `:ret`, `:fn`) across all public functions. | Rigorous invariant enforcement relating input arguments to conformed returns. |
| [`:clj.spec/naming-as-indirection`](#clj-014) | **CLJ-014** | `(separate :name/sign :name/sense)` | Fregean separation: Sign (keyword), Sense (spec), Referent (map). | The foundational indirection axiom preventing implementation leaks. |
| [`:clj.spec/namespaced-sovereignty`](#clj-015) | **CLJ-015** | `(enforce :ns/qualified-keyword)` | Mandatory two-segment qualified keywords for all semantic coordinates. | Prevents coordinate collision across orthogonal research tracks. |
| [`:clj.spec/narrow-naming`](#clj-016) | **CLJ-016** | `(constrain :name/sense :narrow)` | Tellman narrowness: names reveal their sense while excluding non-conforming types. | Pure transformations use nouns; predicates end in `?`; no leaking of referent types. |
| [`:clj.spec/hypothetical-constancy`](#tell-1001) | **TELL-1001** | `(fix-sense :hypothetical)` | Fixing timeless senses in Mode-D registry as coordination landmarks. | The necessary anchor for deductive reasoning. |
| [`:clj.spec/deductive-boundary`](#tell-1002) | **TELL-1002** | `(partition-logic :D :S)` | The hard partition between Mode-D (Deductive Hub) and Mode-S (Stochastic/Corpus). | Grounding `seme` strictly in deterministic in-memory logic. |

---

## 2. Deep Projections & Architectural Grounding

<a id="clj-001"></a>
### CLJ-001: Predication as First-Class Composition (`:clj.spec.op/predication`)
* **Logic:** $\forall x \, (\text{Spec}(x) \iff \text{IsPredicateComposition}(x))$
* **Hickey Invariant:** A specification is not a nominal type or class definition; it is a logical composition of boolean predicates over pure values.
* **Projection in `seme.rheme`:** Reifies the Peircean unsaturated predicate. A seme (`rheme/seme`) wraps a predicate function with a sovereign coordinate identifier (`:seme/id`) and metadata, treating predication as an inspectable entity before it is ever applied to an empirical subject.

<a id="clj-001-a"></a>
<a id="clj-001-o"></a>
### CLJ-001-A / CLJ-001-O: Conjunction & Tagged Disjunction
* **Logic:**
  $$(s/\text{and} \; p_1 \; p_2) \implies \text{Valid}(x) \iff \big(p_1(x) \land p_2(p_1\text{-conform}(x))\big)$$
  $$(s/\text{or} \; k_1 \; p_1 \; k_2 \; p_2) \implies \text{Valid}(x) \iff \big(p_1(x) \lor p_2(x)\big) \quad \text{with tagged path } k_i$$
* **Hickey Invariant:** Conjunction re-hydrates data through sequential transformations, while disjunction tags the branching path to prevent downstream guess-work.
* **Projection in `seme.pheme`:** Conformed witnesses retain tagged paths from `s/or`, allowing analytical pipelines to deterministically distinguish between alternative linguistic or epistemic interpretations without re-evaluating predicates.

<a id="clj-002"></a>
<a id="clj-004"></a>
### CLJ-002 / CLJ-004: Keyset Membership & The Open-World Assumption
* **Logic:**
  $$\forall m \, (\text{IsMap}(m) \implies \text{Keys}(m) \supseteq \text{ReqKeys})$$
  $$\forall m \, \forall k \notin \text{ReqKeys} \, (k \in \text{Keys}(m) \implies \text{Valid}(m))$$
* **Hickey Invariant:** Maps do not have "types." Maps are associative environments where attributes accrete over time. `s/keys` checks only for presence of required coordinates; unmentioned keys are ignored.
* **Projection in `seme.pheme` (`SEME-ARCH-002`):** Directly enables **Multi-Vertex Co-Projection**. Orthogonal disciplines project onto a single referent map:
  ```clojure
  ;; One referent map admitted by multiple orthogonal vertex specs
  (def referent
    {:ling/id "e-101"
     :phon/ipa "/rʌn/"
     :syn/pos :verb
     :feat/aspect :telic
     :phil/canonical-doi "10.1000/182"})

  (pheme/admits-vertex? :vertex/phonology referent)      ;=> true
  (pheme/admits-vertex? :vertex/morphosyntax referent)   ;=> true
  (pheme/admits-vertex? :vertex/philology referent)      ;=> true
  ```
  Neither vertex subclasses the other, and neither vertex strips the other's keys.

<a id="clj-003"></a>
<a id="clj-015"></a>
### CLJ-003 / CLJ-015: Global Registry Constancy & Sovereign Coordinates
* **Logic:** $\forall x \, (\text{Coordinate}(x) \implies \text{HasNamespace}(x))$
* **Hickey Invariant:** Meaning belongs to the namespaced keyword, not the container. An attribute like `:user/email` or `:seme.feat/telic` has a single, timeless sense across the entire system.
* **Projection in `seme.rheme` (`SEME-ARCH-001`):** Every semantic feature (seme) must carry a sovereign two-segment namespaced keyword (e.g. `:seme.feat/telic`, `:seme.ont/animate`). Single-segment keywords like `:id` or `:telic` are rejected as stochastic noise.

<a id="clj-006"></a>
<a id="clj-006-i"></a>
### CLJ-006 / CLJ-006-I: Conformance as Re-hydration & Anomaly Extraction
* **Logic:**
  $$s/\text{conform}(P, x) \implies \text{ConformedAST}(x) \lor \text{:clojure.spec.alpha/invalid}$$
  $$\text{IsInvalid}(r) \implies \text{Invoke}(s/\text{explain-data})$$
* **Hickey Invariant:** Validation should never produce boolean blindness. Successful verification parses data into structured witness ASTs; failure extracts precise error coordinates.
* **Projection in `seme.pheme` (`SEME-ARCH-003`):** `pheme/conformed-witness` constructs an immutable proof record:
  ```clojure
  {:witness/spec :vertex/phonology
   :witness/valid? true
   :witness/parsed {:ling/id "e-101" :phon/ipa "/rʌn/"}
   :witness/anatomy (s/form :vertex/phonology)
   :witness/anomaly nil
   :witness/referent referent}
  ```
  If invalid, `pheme/explain-anomaly` extracts the problem count, failing paths, and violated predicates for machine reasoning.

<a id="clj-009"></a>
<a id="clj-009-s"></a>
### CLJ-009 / CLJ-009-S: Generative Leverage & Boundary Shrinking
* **Logic:**
  $$\forall S \, (\text{Spec}(S) \implies \exists g \, (\text{Generator}(g) \land \text{ProducesValid}(g, S)))$$
  $$\text{Fails}(x) \implies \text{Minimize}(x, \text{test.check/shrink})$$
* **Hickey Invariant:** A specification without generative execution is passive documentation. Generative testing shifts example creation from human bias to machine randomization, while shrinking strips noise to isolate root causes.
* **Projection in `seme.spec` & `seme.delome` (`SEME-ARCH-007`):**
  * `seme.spec` binds custom dense generators to sovereign attributes to prevent infinite-entropy dilution.
  * `delome/boundary-specimen?` uses shrinking to identify minimal referents that satisfy Vertex $V_1$ while falsifying Vertex $V_2$, systematically mapping theoretical boundaries.

<a id="clj-012"></a>
### CLJ-012: The Function Triad Contract (`:clj.spec.fn/fdef`)
* **Logic:** $\text{Contract}(f) \iff \big(\text{Args}(f) \land \text{Ret}(f) \land \text{Fn}(f)\big)$
* **Hickey Invariant:** Verifying argument and return shapes is insufficient; the contract must enforce the relational invariant linking inputs to outputs.
* **Projection across `phosphene.seme.spec`:**
  * `rheme/plerematic-difference`: Proves that $(A \setminus B) \cup (B \setminus A) \cup (A \cap B) = A \cup B$.
  * `pheme/conformed-witness`: Proves that `:witness/referent` is bitwise identical to the input referent (zero mutation).
  * `delome/infer-candidate-spec`: Proves that inferred required keys appear in 100% of corpus specimens.

<a id="clj-014"></a>
<a id="clj-016"></a>
### CLJ-014 / CLJ-016: Fregean Indirection & Tellman Narrow Naming
* **Logic:**
  $$\forall n \, \big(\text{Name}(n) \implies \text{Sign}(n) \land \text{Sense}(n) \land \text{Referent}(n)\big)$$
  $$\forall s \, \big(\text{Sense}(s) \implies \text{Narrow}(s)\big)$$
* **Hickey & Tellman Invariant:** The Sign is the identifier; the Sense is the timeless specification; the Referent is the transient implementation. Pure transformations within scope avoid verbs; cross-boundary operations require verbs; implementation mechanics must never leak into signs.
* **Projection across `seme`:**
  * Pure transformations: `plerematic-difference`, `conformed-witness`, `contextual-overlay`.
  * Predicates: `seme?`, `sememe?`, `witness?`, `admits-vertex?`, `boundary-specimen?`.
  * Senses are narrow: `seme` represents an unsaturated feature; `sememe` represents a feature bundle; `witness` represents a proof-carrying verification result.

<a id="tell-1001"></a>
<a id="tell-1002"></a>
### TELL-1001 / TELL-1002: Hypothetical Constancy & The Deductive Hard Partition
* **Logic:**
  $$\forall t \, \exists S \, \big(\text{Sense}(S) \land \text{Invariant}(S, t)\big)$$
  $$\forall p \, \big(\text{Deductive}(p) \implies p \in \text{Mode-D} \land p \notin \text{Mode-S}\big)$$
* **Hickey & Phosphene Invariant:** We cannot deduce to a stochastic process. Mode-D deductive engines must be self-contained, deterministic, and hermetically isolated from empirical noise.
* **Projection in `seme`:**
  `phosphene/seme` has zero network dependencies, zero database hooks, and zero disk I/O. It operates entirely as an in-memory algebraic engine for symbolic predication. Mode-S empirical corpora enter only through the inductive lifting bridge (`delome/infer-candidate-spec`), which distills observed regularities into formal candidate specs without polluting the deductive core.
