---
uri: seme/doc/wiki/figures-and-transgression
title: Roland Barthes in phosphene/seme: Nomadic Figures and Symbolic Transgression
owner: edphos
status: living
updated: 2026-11-15
description: "Formalizing Roland Barthes' nomadic figures (semic constellations around proper names) and symbolic transgression across semiotic square contrariety in phosphene/seme."
---

# Nomadic Figures & Symbolic Transgression in `phosphene/seme`

> *"The figure is not a personage, it is a nomadic configuration of semes... It is a space of migration where meanings gather and dissolve."*  
> — Roland Barthes, *S/Z* (1970, §XXVIII)

> *"Antithesis is the sacred barrier separating contraries. To cross it directly without passing through mediating negation is to commit symbolic transgression."*  
> — Roland Barthes, *S/Z* (1970, §XLI, §LXXXI)

`phosphene.seme.figure` complements `phosphene.seme.actant`'s syntactic roles (`Subject`, `Object`, etc.) with **the Figure**—a topological locus through which connotative semes migrate across discourse—and formalizes **Symbolic Transgression** across Greimassian semiotic squares.

---

## 1. The Barthesian Figure (`make-figure`, `figure?`)

Unlike an actant (which is an invariant structural slot in a narrative program), a **Figure** is an open semantic magnet:
- Tied to a referent actor identity (`:figure/actor`).
- Gathers an evolving set of qualitative semes (`:figure/semic-constellation`).
- Accumulates a time-indexed path across analytical coordinates (`:figure/trajectory`).

```clojure
(require '[phosphene.seme.core :as seme])

(def fig (seme/make-figure :fig/sarrasine :actor/sarrasine #{:impulsive :artist}))
(def fig2 (seme/accumulate-semes fig #{:tragic :passion}))

(:figure/semic-constellation fig2)
;;=> #{:impulsive :artist :tragic :passion}
```

---

## 2. Tracing Semic Trajectories over Discourse (`trace-figure`)

`trace-figure` slides across an ordered sequence of lexias, extracting qualitative semes co-occurring with the figure, and projecting each step onto a coordinate or semiotic square:

```clojure
(def traced (seme/trace-figure fig lexias square-projection-fn))

(count (:figure/trajectory traced)) ;=> 2
(first (:figure/trajectory traced))
;;=> {:trajectory/step 0,
;;    :lexia/id :doc:p:1#L0,
;;    :semes #{:art},
;;    :projection {:projection/position :s1, :projection/term :art}}
```

---

## 3. Lawful Dialectical Mediation vs. Symbolic Transgression (`detect-transgression`)

In Greimas's canonical narrative grammar, progressing from contrary pole $S_1$ to $S_2$ requires traversing mediating contradictory negation:
$$S_1 \xrightarrow{\quad\text{negation}\quad} \sim S_1 \xrightarrow{\quad\text{assertion}\quad} S_2$$

In Barthes' symbolic code (`:code/symbolic`), narrative crisis arises from **Transgression**:
1. **Direct Contrary Leap:** Jumping from $S_1 \longleftrightarrow S_2$ without passing through $\sim S_1$ or $\sim S_2$.
2. **Transgressive Collapse:** Stepping directly from a contrary pole ($S_1$ or $S_2$) into the Complex synthesis ($S_1 + S_2$) without dialectical mediation.

```clojure
;; Lawful mediation: S_1 -> ~S_1
(seme/detect-transgression sq :masculine :non-masculine)
;;=> {:transgression? false, :type :lawful-mediation, ...}

;; Illicit leap: S_1 -> S_2
(seme/detect-transgression sq :masculine :feminine)
;;=> {:transgression? true, :type :direct-contrary-leap, ...}

;; Transgressive collapse: S_1 -> Complex term
(seme/detect-transgression sq :masculine :complex)
;;=> {:transgression? true, :type :transgressive-collapse, ...}
```
