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
| `:seme.ont/semic-articulation` | SEME-001 | `(reify :peirce/firstness :spec/seme)` | Unsaturated Qualitative Sign |
| `:seme.ont/open-predication` | SEME-002 | `(bundle :map/open-world :spec/affordances)` | Open-World Predicative Bundle |
| `:seme.pheme/secondness-encounter` | SEME-003 | `(encounter :perimeter/raw :peirce/secondness)` | Brute Indexical Shock |
| `:seme.pheme/perceptual-judgment` | SEME-004 | `(subsume :peirce/secondness :peirce/thirdness)` | Phemic Perceptual Gate |
| `:seme.ont/predicative-priority` | SEME-005 | `(order :act/predication :act/prediction)` | Predication Precedes Prediction |
| `:seme.delome/constant-conjunction` | SEME-006 | `(distill :mode-s/impressions :habit/candidate)` | Inductive Custom |
| `:seme.delome/customary-transition` | SEME-007 | `(chain :predication/antecedent :expectation/consequent)` | Habitual Transition |
| `:seme.delome/non-necessary-prediction` | SEME-008 | `(forbid :deduction/necessity :projection/future)` | Humean Modal Contingency |
| `:seme.delome/provisional-constancy` | SEME-009 | `(stabilize :habit/custom :sense/hypothetical)` | Grounded Constancy (`TELL-1001`) |
| `:seme.pheme/diagnostic-surprise` | SEME-010 | `(surface :conform/invalid :abduction/anomaly)` | Epistemic Surprise |
| `:seme.delome/collapsed-abduction` | SEME-011 | `(frame :spec/sense :hypothesis/prospective)` | Embryonic Abduction |
| `:seme.delome/pragmatic-conditional` | SEME-012 | `(bind :fn/encounter :consequence/would-be)` | Delomic Pragmatic Would-Be |
| `:seme.vertex/polysemic-projection` | SEME-013 | `(compose :registry/sovereign :vertex/overlay)` | Contextual Vertex Refinement |
| `:seme.vertex/epistemic-loop` | SEME-014 | `(cycle :seme :pheme :delome :prediction :surprise)` | Living Habit Loop |

---

## 2. Mathematical Formalization

### A. Predicative Priority
$$\forall x \, \forall \Pi \, \Big(\text{Predicts}(x, \Pi) \implies \exists \Phi \, \big(\text{Predicates}(x, \Phi) \wedge \text{Precedes}(\Phi, \Pi)\big)\Big)$$

### B. Humean Modal Contingency
$$\forall H \, \forall t_1 \, \forall t_2 \, \Big(\big(t_1 < t_2 \wedge \text{ConstantConjunction}(H, t_1)\big) \implies \big(\sim\Box \text{Holds}(H, t_2) \wedge \Diamond \sim\text{Holds}(H, t_2)\big)\Big)$$

Across the Mode-D / Mode-S boundary (`TELL-1002`), validation failure against a delomic expectation is not a fatal code bug; it is **Peircean Diagnostic Surprise** that pinpoints the failing coordinate via `s/explain-data` and triggers abductive habit revision.
