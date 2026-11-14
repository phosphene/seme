---
uri: seme/doc/wiki/greimassian-semiotics
title: Algirdas Julien Greimas in phosphene/seme: Actantial Syntax & Narrative Programs
owner: edphos
status: living
updated: 2026-11-14
description: "Formalizing Greimas's Actantial Model, state junctions (S ^ O / S v O), narrative programs, and canonical schemas in Clojure."
---

# Greimassian Semiotics in `phosphene/seme`: Actantial Syntax & Narrative Programs

> *"Narrative syntax is fundamentally an algebra of state transformations operated upon actantial relations."*  
> — Algirdas Julien Greimas, *Sémantique structurale* (1966) & *Du Sens* (1970)

While `phosphene/paradigma.greimas` formalizes the **Semiotic Square** on the paradigmatic content plane, `phosphene/seme.actant` formalizes **Actantial Syntax and Narrative Transformations** on the predicative plane.

---

## 1. The Actantial Model (`actantial-model`)

Greimas de-complected theatrical characters and dramatis personæ into **6 structural actantial roles** distributed across three orthogonal relational axes:

1. **Axis of Desire / Volition:**
   $$\text{Subject} \longleftrightarrow \text{Object}$$
2. **Axis of Transmission / Communication:**
   $$\text{Sender (Destinateur)} \longrightarrow \text{Object} \longrightarrow \text{Receiver (Destinataire)}$$
3. **Axis of Power / Auxiliary Testing:**
   $$\text{Helper (Adjuvant)} \longleftrightarrow \text{Subject} \longleftrightarrow \text{Opponent (Opposant)}$$

### Actor-Actant Syncretism
In `phosphene.seme.actant`, actants are formal positions, while actors are referents that can embody multiple actantial positions simultaneously (e.g. in shamanic or initiation journeys, the Subject is also the Sender and Receiver):

```clojure
(require '[phosphene.seme.core :as seme])

(def model (seme/actantial-model {:subject :actor/hero
                                  :object :actor/golden-fleece
                                  :sender :actor/king-pelias
                                  :receiver :actor/thessaly
                                  :helper [:actor/medea :actor/argonauts]
                                  :opponent [:actor/dragon :actor/aeetes]}))

(seme/find-roles-for-actor model :actor/medea)
;;=> #{:helper}
```

---

## 2. Narrative States & Junctions (`narrative-state`)

A narrative state is the qualitative junction between a Subject and an Object:
- **Conjunction ($S \wedge O$):** Possession, communion, fulfillment.
- **Disjunction ($S \vee O$ or $S \setminus O$):** Lack, separation, alienation.

```clojure
(def initial-lack (seme/narrative-state :actor/jason :disjunction :item/golden-fleece))
(def final-gain   (seme/narrative-state :actor/jason :conjunction :item/golden-fleece))

(seme/disjoined? initial-lack)
;;=> true
```

---

## 3. Narrative Programs (`narrative-program`)

An elementary Narrative Program ($PN$) formalizes the transformation of state relations:
$$PN = \mathcal{F}_{\text{trans}} \Big( S_{\text{op}} \to [ S_1 \vee O \Longrightarrow S_1 \wedge O ] \Big)$$

Transformations are strictly typed:
- `:acquisition` ($S \vee O \to S \wedge O$)
- `:deprivation` ($S \wedge O \to S \vee O$)

```clojure
(def quest-pn (seme/narrative-program :pn/retrieve-fleece :actor/jason initial-lack final-gain))

(seme/execute-program quest-pn initial-lack)
;;=> {:status :success,
;;    :pn/id :pn/retrieve-fleece,
;;    :state/previous {:state/subject :actor/jason, :state/junction :disjunction, ...},
;;    :state/current  {:state/subject :actor/jason, :state/junction :conjunction, ...}}
```

---

## 4. Canonical Narrative Schema (`canonical-trajectory`)

Evaluates whether a sequence of mythic or empirical events conforms to Greimas's 4 invariant narrative phases:
1. `:manipulation` (Contract / Mandate)
2. `:competence` (Acquisition of modal capacity: wanting, having, knowing, being able to do)
3. `:performance` (The central transformation)
4. `:sanction` (Evaluation and recognition)
