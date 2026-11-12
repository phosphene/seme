---
uri: seme/wiki/genealogy
title: Philosophical and Linguistic Genealogy of phosphene/seme
owner: edphos
status: living
updated: 2026-11-12
description: "Tracing C.S. Peirce's 1906 Prolegomena trichotomy and European structuralist componential semantics to Rich Hickey's clojure.spec.alpha."
---

# Philosophical & Linguistic Genealogy of `phosphene/seme`

`phosphene/seme` occupies a singular position in computational ontology by formalizing the convergence of two foundational 20th-century semantic insights with modern homoiconic software design.

---

## 1. C.S. Peirce's 1906 Prolegomena Trichotomy

In his 1906 work *Prolegomena to an Apology for Pragmaticism* (CP 4.538), Charles Sanders Peirce refined his earlier semiotic divisions (specifically the rheme / dicisign / argument triad) to free the unsaturated predicate from sentential syntax:

> *"A Seme is any sign which serves for its Interpretant as a sign of an object for some possibility... A Pheme is a sign which is equivalent to a grammatical proposition... A Delome is a sign which has the form of being an argument, or sign of an inferential habit."*  
> — C.S. Peirce (1906, CP 4.538)

```
Peircean Triad (1906)
  ├── Seme   (Firstness)  → Unsaturated qualitative affordance ("___ burns", "___ is telic")
  ├── Pheme  (Secondness) → Propositional saturation asserting a state of affairs
  └── Delome (Thirdness)  → Inferential rule or habit governing valid transitions
```

In `phosphene/seme`:
* **Firstness (`seme.rheme`)**: An unsaturated predicate or distinctive semantic feature prior to evaluation.
* **Secondness (`seme.pheme`)**: The empirical evaluation of an open referent against a predicate, producing a conformed witness.
* **Thirdness (`seme.delome`)**: The inferential habit, contextual overlay, or candidate schema derived from regular observation.

---

## 2. European Structuralist Componential Semantics

Parallel to Peirce's semiotic triad, mid-century European structural linguistics (Eric Buyssens, Bernard Pottier, Algirdas Julien Greimas, and Eugenio Coseriu) established **componential analysis** to decompose lexical meaning into minimal distinctive semantic units:

* **Seme (Minimal Distinctive Feature):** The smallest distinctive unit of the content plane, analogous to the phonological distinctive feature (e.g. `[+animate]`, `[+human]`, `[-adult]` in defining *boy* vs. *man*).
* **Sememe (Feature Bundle):** The collection of semes that constitute the semantic content of a specific lexeme or sign.
* **Archisememe:** The intersection of shared semes across a lexical field.

In `phosphene.seme.rheme`, this is formalized as pure set algebra over sovereign namespaced keywords:
$$\text{sememe}(S_1) \cap \text{sememe}(S_2) \implies \text{shared semes}$$
$$\text{sememe}(S_1) \mathbin{\Delta} \text{sememe}(S_2) \implies \text{plerematic difference}$$

---

## 3. The Hickey Critique of Nominal Hierarchy

In object-oriented programming and nominal schema systems, types are modeled as closed, monolithic containers (`class User { id: UUID; email: String }`). Rich Hickey identified three fatal flaws in this model:

1. **Compounding Attributes with Containers:** Why is an email address a "property of User" rather than an independent concept with its own universal invariants?
2. **Boolean Blindness:** Validation functions return `true` or `false`, throwing away the structural insight discovered during validation.
3. **Closed-World Brittleness:** Adding attributes breaks downstream consumers; systems require artificial subclassing or multiple inheritance.

`clojure.spec.alpha` resolves this by binding predicates to **sovereign namespaced keywords** (`:user/email`, `:seme.feat/telic`) evaluated over open-world hash maps (`s/keys`).

`phosphene/seme` takes Hickey's solution and reifies it as a formal laboratory: studying how independent semantic features (semes) bundle into perspectives, project across open maps as multiple simultaneous vertices, and reify proof-carrying witnesses.
