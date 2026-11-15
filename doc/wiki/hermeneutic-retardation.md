---
uri: seme/doc/wiki/hermeneutic-retardation
title: Roland Barthes & A.J. Greimas: Hermeneutic Retardation on the Veridictory Square
owner: edphos
status: living
updated: 2026-11-15
description: "Formalizing Roland Barthes' hermeneutic code, retardation morphemes (dilatoire), and Greimas's Veridictory Square state machine in phosphene/seme."
---

# Hermeneutic Retardation on the Veridictory Square in `phosphene/seme`

> *"The hermeneutic code consists in timely formulating, suspending, and ultimately resolving an enigma... Discourse sustains itself by deferring the answer through a sequence of dilatory morphemes."*  
> — Roland Barthes, *S/Z* (1970, §XXXII, §XXXVII, §LXXXIX)

> *"The veridictory status of any discourse is determined by the double category of Being and Seeming."*  
> — Algirdas Julien Greimas, *Du Sens II* (1983)

`phosphene.seme.hermeneutic` formalizes the structural coupling between **Roland Barthes' Hermeneutic Code** (*code herméneutique*) and **Greimas's Veridictory Square** (*carré de la véridiction*), modeling truth disclosure as a non-trivial, guarded epistemic trajectory.

---

## 1. The Hermeneutic Morphemes & Veridictory Mapping

Barthes catalogued the inventory of morphemes that open, delay, and conclude an enigma:

| Barthesian Morpheme (*S/Z* §LXXXIX) | Epistemic Role | Veridictory Position |
| :--- | :--- | :--- |
| `:hermeneutic/thematization` | Initial suggestion of an unresolved topic | Neutral / Unformulated |
| `:hermeneutic/enigma` | Formal formulation of the question ($S \vee O_{\text{truth}}$) | `:secret` ($S_1 + \sim S_2$) |
| `:hermeneutic/promise` | Declaration that a resolution exists | `:secret` |
| `:hermeneutic/snare` (*leurre*) | Deliberate deception or misleading appearance | **`:lie`** ($S_2 + \sim S_1$, Seeming without Being) |
| `:hermeneutic/equivocation` (*équivoque*) | Double-edged answer suspending commitment | Suspended `:truth` ($S_1 + S_2$) |
| `:hermeneutic/jamming` (*blocage*) | Declaration of insolubility | **`:falsity`** ($\sim S_1 + \sim S_2$, Non-Being & Non-Seeming) |
| `:hermeneutic/suspended-answer` | Temporary abeyance of inquiry | Retarded `:secret` |
| `:hermeneutic/partial-answer` | Vertical deixis step disclosing clue without total closure | **`:secret`** ($S_1 + \sim S_2$, Being without full Seeming) |
| `:hermeneutic/disclosure` (*dévoilement*) | Irreversible final convergence | **`:truth`** ($S_1 + S_2$, Being + Seeming) |

---

## 2. Structural Delay & Retardation Ratio (`retardation-ratio`)

The **dilatory ratio** (*dilatoire*) measures how many non-closing, suspensive morphemes intervene per posed enigma before disclosure is achieved:

$$\text{Retardation Ratio} = \frac{\sum \text{Retardation Morphemes}}{\sum \text{Enigmas Posed}}$$

In empirical science and mystery narratives, this ratio distinguishes trivial tautologies ($\text{ratio} = 0.0$) from dense exploratory investigations ($\text{ratio} \gg 1.0$).

---

## 3. Guarding Canonical Sanction (`guard-sanction`)

In `phosphene.seme.actant`, a narrative schema proceeds through:
$$\text{:manipulation} \longrightarrow \text{:competence} \longrightarrow \text{:performance} \longrightarrow \text{:sanction}$$

When the Object of Value is verified knowledge ($S \vee O_{\text{truth}}$), **the trajectory must not be permitted to enter `:sanction` prematurely**:
- If an agent or analyst attempts to sanction a claim while the hermeneutic trajectory is still in `:snare`, `:equivocation`, `:jamming`, or an open unverified state, `guard-sanction` intercepts the execution, strips `:sanction`, and returns `:status :blocked`.
- Only upon explicit `:hermeneutic/disclosure` does `:sanction-ready?` become `true`, permitting full narrative closure.

```clojure
(require '[phosphene.seme.core :as seme])

;; 1. Attempt premature closure with an active snare
(def blocked
  (seme/guard-sanction
    {:manipulation "Contract" :competence "Apparatus" :performance "Observation" :sanction "Truth claim"}
    [:hermeneutic/enigma :hermeneutic/snare]))

(:status blocked) ;=> :blocked
(:reason blocked) ;=> :premature-sanction-attempted

;; 2. Legitimate closure following full disclosure
(def permitted
  (seme/guard-sanction
    {:manipulation "Contract" :competence "Apparatus" :performance "Observation" :sanction "Truth claim"}
    [:hermeneutic/enigma :hermeneutic/partial-answer :hermeneutic/disclosure]))

(:status permitted) ;=> :permitted
```
