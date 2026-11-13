---
uri: seme/wiki/proto-predication-and-prediction
title: Seme, Pheme, Delome: Proto-Predication and Contingent Prediction (v1.2-Peirce-Hume-Omega)
owner: edphos
status: living
updated: 2026-11-13
description: "Theoretical framework and formal specification for predicative priority, Humean customary transitions, and Peircean diagnostic surprise."
---

# Proto-Predication and Contingent Prediction (`v1.2-Peirce-Hume-Omega`)

In classical type systems and computational schema libraries, data validation is treated as a static Boolean gate: data either matches a pre-existing type or raises a fatal defect.

`phosphene/seme` departs fundamentally from this paradigm by grounding computational semiotics in **C.S. Peirce's 1906 trichotomy** (Seme, Pheme, Delome; CP 4.538) and **David Hume's critique of induction**:

1. **Predication Precedes Prediction (`SEME-005`):** Before an apparatus can project any delomic expectation onto future experience, it must first articulate unsaturated qualitative affordances (**Seme** / Firstness) and enact an immediate perceptual judgment (**Pheme** / subsuming singular Secondness under Thirdness).
2. **Modal Non-Necessity of Prediction (`SEME-008`, `TELL-1002`):** Transitions from past constant conjunctions to future expectations (**Delome** / Thirdness) are governed by contingent custom and hypothetical constancy (`TELL-1001`) across the deductive boundary (`TELL-1002`), never by deductive entailment.

---

## 1. The Epistemic Cycle & Coordinate Topology

```
             Mode-S Singular Encounter (Secondness)
                          │
                          ▼
            [Phemic Perceptual Gate] ──(Invalid)──► Stage 1 Failure
                          │                        (Ingress Grounding Anomaly)
                      (Conforms)
                          │
                          ▼
           [Contingent Delomic Habit] ──(Invalid)──► Diagnostic Surprise (~Box H)
                          │                         (Abductive Revision Signal)
                      (Conforms)
                          │
                          ▼
               Delomic Habit Borne Out
```

### Complete Axiom Coordinates (`SEME-001` through `SEME-014`)

| Coordinate | ID | Predicate | Nuance Anchor |
| :--- | :--- | :--- | :--- |
| `:seme.ont/semic-articulation` | [**SEME-001**](#seme-001) | `(reify :peirce/firstness :spec/seme)` | Unsaturated Qualitative Sign |
| `:seme.ont/open-predication` | [**SEME-002**](#seme-002) | `(bundle :map/open-world :spec/affordances)` | Open-World Predicative Bundle |
| `:seme.pheme/secondness-encounter` | [**SEME-003**](#seme-003) | `(encounter :perimeter/raw :peirce/secondness)` | Brute Indexical Shock |
| `:seme.pheme/perceptual-judgment` | [**SEME-004**](#seme-004) | `(subsume :peirce/secondness :peirce/thirdness)` | Phemic Perceptual Gate |
| `:seme.ont/predicative-priority` | [**SEME-005**](#seme-005) | `(order :act/predication :act/prediction)` | Predication Precedes Prediction |
| `:seme.delome/constant-conjunction` | [**SEME-006**](#seme-006) | `(distill :mode-s/impressions :habit/candidate)` | Inductive Custom |
| `:seme.delome/customary-transition` | [**SEME-007**](#seme-007) | `(chain :predication/antecedent :expectation/consequent)` | Habitual Transition |
| `:seme.delome/non-necessary-prediction` | [**SEME-008**](#seme-008) | `(forbid :deduction/necessity :projection/future)` | Humean Modal Contingency |
| `:seme.delome/provisional-constancy` | [**SEME-009**](#seme-009) | `(stabilize :habit/custom :sense/hypothetical)` | Grounded Constancy (`TELL-1001`) |
| `:seme.pheme/diagnostic-surprise` | [**SEME-010**](#seme-010) | `(surface :conform/invalid :abduction/anomaly)` | Epistemic Surprise |
| `:seme.delome/collapsed-abduction` | [**SEME-011**](#seme-011) | `(frame :spec/sense :hypothesis/prospective)` | Embryonic Abduction |
| `:seme.delome/pragmatic-conditional` | [**SEME-012**](#seme-012) | `(bind :fn/encounter :consequence/would-be)` | Delomic Pragmatic Would-Be |
| `:seme.vertex/polysemic-projection` | [**SEME-013**](#seme-013) | `(compose :registry/sovereign :vertex/overlay)` | Contextual Vertex Refinement |
| `:seme.vertex/epistemic-loop` | [**SEME-014**](#seme-014) | `(cycle :seme :pheme :delome :prediction :surprise)` | Living Habit Loop |

---

## 2. Complete Coordinate Specifications (`SEME-001` through `SEME-014`)

<a id="seme-001"></a>
### SEME-001: Unsaturated Qualitative Sign (`:seme.ont/semic-articulation`)
* **Semiotic Dimension:** Firstness (Pure Quality / Possibility).
* **Predicate:** `(reify :peirce/firstness :spec/seme)`
* **Formal Logic:**
  $$\forall S \, \Big(\text{IsSeme}(S) \implies \big(\text{Unsaturated}(S) \wedge \text{Firstness}(S) \wedge \text{Spec}(S)\big)\Big)$$
* **Definition & Grounding:**
  Reifies the unsaturated qualitative predicate before it is bound to an empirical subject. Corresponds to Peirce's Seme (CP 4.538)—a sign representing an object for some pure possibility (e.g. `(s/and number? (complement neg?))` representing `:seme.phenom/luminosity`). It exists as an autonomous dimension of reality without demanding a pre-allocated container.

<a id="seme-002"></a>
### SEME-002: Open-World Predicative Bundle (`:seme.ont/open-predication`)
* **Semiotic Dimension:** Firstness into Secondness (Co-presence of affordances).
* **Predicate:** `(bundle :map/open-world :spec/affordances)`
* **Formal Logic:**
  $$\forall m \, \forall \mathcal{K} \, \Big(\text{AdmitsBundle}(m, \mathcal{K}) \iff \big(\text{IsMap}(m) \wedge \mathcal{K} \subseteq \operatorname{keys}(m)\big)\Big)$$
* **Definition & Grounding:**
  Implements the Open-World Assumption (`CLJ-004`). A referent is not a closed nominal class, but an open associative map capable of accreting orthogonal predicates across disparate disciplines without schema collisions.

<a id="seme-003"></a>
### SEME-003: Brute Indexical Shock (`:seme.pheme/secondness-encounter`)
* **Semiotic Dimension:** Secondness (The Raw Empirical Encounter).
* **Predicate:** `(encounter :perimeter/raw :peirce/secondness)`
* **Formal Logic:**
  $$\forall x \, \Big(\text{EmpiricalImpression}(x) \implies \text{Secondness}(x) \wedge \text{Unnamespaced}(x)\Big)$$
* **Definition & Grounding:**
  The unmediated impact of the world at the system's stochastic perimeter (`Mode-S`). It represents raw, unnamespaced data (JSON payloads, sensor arrays, external tokens) before any conceptual recognition or spec gating has taken place.

<a id="seme-004"></a>
### SEME-004: Phemic Perceptual Gate (`:seme.pheme/perceptual-judgment`)
* **Semiotic Dimension:** Secondness subsumed under Thirdness (Propositional saturation).
* **Predicate:** `(subsume :peirce/secondness :peirce/thirdness)`
* **Formal Logic:**
  $$\forall x \, \forall S \, \Big(\text{Conforms}(x, S) \implies \text{WitnessAST}(\text{s/conform}(S, x))\Big)$$
* **Definition & Grounding:**
  Peirce's Pheme (CP 4.538) and perceptual judgment. The singular encounter ($x$) is subsumed under an established predicate ($S$), yielding a proof-carrying conformed witness (`pheme/conformed-witness`, `CLJ-006`). If the gate fails, an ingress anomaly is reported immediately.

<a id="seme-005"></a>
### SEME-005: Predicative Priority (`:seme.ont/predicative-priority`)
* **Semiotic Dimension:** Fundamental Ontological Ordering ($\Phi \prec \Pi$).
* **Predicate:** `(order :act/predication :act/prediction)`
* **Formal Logic:**
  $$\forall x \, \forall \Pi \, \Big(\text{Predicts}(x, \Pi) \implies \exists \Phi \, \big(\text{Predicates}(x, \Phi) \wedge \text{Precedes}(\Phi, \Pi)\big)\Big)$$
* **Definition & Grounding:**
  The foundational claim: **Predication precedes prediction**. Prediction—generating an expected future state and minimizing deviation—presupposes the conceptual capacity to predicate qualities of an object. A system cannot anticipate what it cannot predicate.

<a id="seme-006"></a>
### SEME-006: Inductive Custom (`:seme.delome/constant-conjunction`)
* **Semiotic Dimension:** Secondness transitioning to Habit (Inductive distillation).
* **Predicate:** `(distill :mode-s/impressions :habit/candidate)`
* **Formal Logic:**
  $$\forall \mathcal{D} \, \Big(\text{ObservationCorpus}(\mathcal{D}) \implies \text{CandidateSpec}(\operatorname{infer-candidate-spec}(\mathcal{D}))\Big)$$
* **Definition & Grounding:**
  Hume's constant conjunction. Repeated empirical observations of correlated features distill an inductive expectation (`delome/infer-candidate-spec`, `SEME-ARCH-006`), synthesizing candidate required keysets and primitive value constraints.

<a id="seme-007"></a>
### SEME-007: Habitual Transition (`:seme.delome/customary-transition`)
* **Semiotic Dimension:** Thirdness as Operative Custom.
* **Predicate:** `(chain :predication/antecedent :expectation/consequent)`
* **Formal Logic:**
  $$\forall x \, \forall H \, \Big(\big(\text{Conforms}(x, H_{\text{antecedent}}) \wedge \text{Habit}(H)\big) \implies \text{Expects}(x, H_{\text{consequent}})\Big)$$
* **Definition & Grounding:**
  The mental and computational bridge whereby the mind, determined by custom, passes from the impression of one object to the expectation of its usual attendant (`delome/customary-transition`). It connects a current phemic judgment to a prospective delomic expectation.

<a id="seme-008"></a>
### SEME-008: Humean Modal Contingency (`:seme.delome/non-necessary-prediction`)
* **Semiotic Dimension:** Modal Logic of Empirical Projection ($\sim\Box H$).
* **Predicate:** `(forbid :deduction/necessity :projection/future)`
* **Formal Logic:**
  $$\forall H \, \forall t_1 \, \forall t_2 \, \Big(\big(t_1 < t_2 \wedge \text{ConstantConjunction}(H, t_1)\big) \implies \big(\sim\Box \text{Holds}(H, t_2) \wedge \Diamond \sim\text{Holds}(H, t_2)\big)\Big)$$
* **Definition & Grounding:**
  Strict Humean modal contingency: no projection from past conjunctions to future instances is logically necessary. In `delome/contingent-prediction`, `:logically-necessary?` is structurally `false`. Empirical predictions remain permanently subject to falsification.

<a id="seme-009"></a>
### SEME-009: Grounded Constancy (`:seme.delome/provisional-constancy`)
* **Semiotic Dimension:** Epistemic Stability across Boundaries (`TELL-1001`).
* **Predicate:** `(stabilize :habit/custom :sense/hypothetical)`
* **Formal Logic:**
  $$\forall k \, \Big(\text{Coordinate}(k) \implies \operatorname{Sense}(k) = \text{Invariant}_{\text{provisional}}\Big)$$
* **Definition & Grounding:**
  Tellman's coordinate constancy (`TELL-1001`). To reason across distributed systems and stochastic boundaries, we must treat coordinates as provisionally constant while remaining conscious that they rest upon empirical habit rather than a priori truth.

<a id="seme-010"></a>
### SEME-010: Epistemic Surprise (`:seme.pheme/diagnostic-surprise`)
* **Semiotic Dimension:** Secondness disrupting Thirdness (Diagnostic anomaly).
* **Predicate:** `(surface :conform/invalid :abduction/anomaly)`
* **Formal Logic:**
  $$\forall x \, \forall H \, \Big(\big(\text{Pheme}(x) \wedge \lnot\text{Conforms}(x, H)\big) \implies \text{Anomaly}(s/\text{explain-data}(H, x))\Big)$$
* **Definition & Grounding:**
  Peircean diagnostic surprise. When a contingent habit fails against a fresh singular encounter, `clojure.spec.alpha` surfaces a structured anomaly map via `s/explain-data`. Failure is not an exception to be swallowed; it is a machine-readable signal demanding model revision.

<a id="seme-011"></a>
### SEME-011: Embryonic Abduction (`:seme.delome/collapsed-abduction`)
* **Semiotic Dimension:** Thirdness under Pressure (Hypothetical reconstruction).
* **Predicate:** `(frame :spec/sense :hypothesis/prospective)`
* **Formal Logic:**
  $$\forall A \, \Big(\text{Anomaly}(A) \implies \text{CandidateHypothesis}(\operatorname{retract-vertex}(A))\Big)$$
* **Definition & Grounding:**
  Peircean abduction framed as deductive contraction. Rather than hallucinating arbitrary new rules, the engine retracts the claimed vertex to the maximal lower bound that remains sound (`pheme/retract-vertex`, `T-SEM-009`), isolating the provoked axioms under tension.

<a id="seme-012"></a>
### SEME-012: Delomic Pragmatic Would-Be (`:seme.delome/pragmatic-conditional`)
* **Semiotic Dimension:** Peircean Thirdness (Pragmatic conditional contracts).
* **Predicate:** `(bind :fn/encounter :consequence/would-be)`
* **Formal Logic:**
  $$\forall f \, \forall x \, \Big(\operatorname{Intervene}(f, x) \implies \operatorname{Spec}_{\text{fn}}\big(\operatorname{conformed}(x), \operatorname{conformed}(f(x))\big)\Big)$$
* **Definition & Grounding:**
  Reifies Peirce's Pragmatic Maxim: the meaning of an intellectual concept consists in what *would be* the sensible consequences of actions performed under specific conditions (`delome/pragmatic-conditional`, `CLJ-012`). Realized in Clojure as `s/fdef` relational invariants.

<a id="seme-013"></a>
### SEME-013: Contextual Vertex Refinement (`:seme.vertex/polysemic-projection`)
* **Semiotic Dimension:** Multi-Tenancy & Selectional Restrictions.
* **Predicate:** `(compose :registry/sovereign :vertex/overlay)`
* **Formal Logic:**
  $$\forall R_{\text{base}} \, \forall R_{\text{vertex}} \, \Big(\text{EffectiveRegistry} = \operatorname{merge}(R_{\text{base}}, R_{\text{vertex}})\Big)$$
* **Definition & Grounding:**
  Permits individual analytical vertices to project localized selectional restrictions (`delome/contextual-overlay`, `SEME-ARCH-005`) over a shared sovereign coordinate registry without polluting global sense.

<a id="seme-014"></a>
### SEME-014: Living Habit Loop (`:seme.vertex/epistemic-loop`)
* **Semiotic Dimension:** The Complete Living Semiotic Cycle.
* **Predicate:** `(cycle :seme :pheme :delome :prediction :surprise)`
* **Formal Logic:**
  $$\operatorname{Cycle}(x) \equiv \operatorname{Seme}(x) \xrightarrow{\Phi} \operatorname{Pheme}(x) \xrightarrow{\Pi} \operatorname{Delome}(x) \xrightarrow{\text{verify}} \{\text{borne-out} \mid \text{surprise}\}$$
* **Definition & Grounding:**
  The integrated epistemic engine (`delome/epistemic-cycle`): ingests raw impressions, executes phemic perceptual gating, projects contingent delomic expectations, and outputs either habit confirmation or a structured diagnostic surprise for human investigation.

---

## 3. Mathematical Formalization

### A. Predicative Priority
$$\forall x \, \forall \Pi \, \Big(\text{Predicts}(x, \Pi) \implies \exists \Phi \, \big(\text{Predicates}(x, \Phi) \wedge \text{Precedes}(\Phi, \Pi)\big)\Big)$$

### B. Humean Modal Contingency
$$\forall H \, \forall t_1 \, \forall t_2 \, \Big(\big(t_1 < t_2 \wedge \text{ConstantConjunction}(H, t_1)\big) \implies \big(\sim\Box \text{Holds}(H, t_2) \wedge \Diamond \sim\text{Holds}(H, t_2)\big)\Big)$$

Across the Mode-D / Mode-S boundary (`TELL-1002`), validation failure against a delomic expectation is not a fatal code bug; it is **Peircean Diagnostic Surprise** that pinpoints the failing coordinate via `s/explain-data` and triggers abductive habit revision.
