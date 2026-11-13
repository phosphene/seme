---
uri: seme/wiki/vertex-lattice
title: Analytical Vertex Lattice and Keyset Subsumption Algebra (CLJ-002)
owner: edphos
status: living
updated: 2026-11-13
description: "Mathematical formulation and Clojure implementation of the bounded vertex lattice and keyset subsumption algebra in phosphene/seme."
---

# Analytical Vertex Lattice & Keyset Subsumption Algebra (`CLJ-002`)

In Rich Hickey's `clojure.spec.alpha` (`CLJ-002`, `CLJ-002-R`, `CLJ-004`), map specifications do not define closed object types. Instead, an `s/keys` spec defines **keyset membership**—asserting that a map provides at least a required set of sovereign coordinates.

`phosphene.seme.pheme` reifies this principle into a formal **bounded lattice / poset** over analytical perspectives.

---

## 1. Formal Mathematical Order

Let $\mathcal{U}_{\text{coord}}$ be the universe of sovereign namespaced coordinate keywords (e.g. `:seme.phon/ipa`, `:seme.syn/pos`). An analytical vertex $V = \langle \text{id}, \text{Req}(V), \text{Opt}(V) \rangle$ is defined by its required coordinate set $\text{Req}(V) \subseteq \mathcal{U}_{\text{coord}}$.

### Partial Order: Keyset Subsumption ($\sqsubseteq$)
For two vertices $V_1$ and $V_2$, $V_1$ subsumes $V_2$ ($V_1 \sqsupseteq V_2$, or `(subsumes? v1 v2)`):
$$V_1 \sqsupseteq V_2 \iff \text{Req}(V_2) \subseteq \text{Req}(V_1)$$

**Theorem (Admission Preservation):**
If $V_1 \sqsupseteq V_2$, then for any open-world referent map $m$:
$$m \models V_1 \implies m \models V_2$$
A more specific analytical perspective (requiring more coordinates) strictly entails all less specific sub-perspectives.

**Lattice Poset Axioms:**
1. **Reflexivity:** $V \sqsupseteq V$.
2. **Anti-Symmetry:** $V_1 \sqsupseteq V_2 \land V_2 \sqsupseteq V_1 \implies \text{Req}(V_1) = \text{Req}(V_2)$.
3. **Transitivity:** $V_1 \sqsupseteq V_2 \land V_2 \sqsupseteq V_3 \implies V_1 \sqsupseteq V_3$.

---

## 2. Lattice Meet ($\bigwedge$) and Join ($\bigvee$)

### Greatest Lower Bound: Vertex Meet ($V_1 \bigwedge V_2$)
The **meet** (`pheme/vertex-meet`) computes the minimal shared consensus between two disciplines—the intersection of required coordinates:
$$\text{Req}(V_1 \bigwedge V_2) = \text{Req}(V_1) \cap \text{Req}(V_2)$$
* **Invariant:** $V_1 \sqsupseteq (V_1 \bigwedge V_2)$ and $V_2 \sqsupseteq (V_1 \bigwedge V_2)$.
* **Meaning:** Any referent map satisfying *either* vertex is guaranteed to satisfy their meet.

### Least Upper Bound: Vertex Join ($V_1 \bigvee V_2$)
The **join** (`pheme/vertex-join`) computes the composite unification of two disciplines—the union of required coordinates:
$$\text{Req}(V_1 \bigvee V_2) = \text{Req}(V_1) \cup \text{Req}(V_2)$$
* **Invariant:** $(V_1 \bigvee V_2) \sqsupseteq V_1$ and $(V_1 \bigvee V_2) \sqsupseteq V_2$.
* **Meaning:** An open referent map must *simultaneously* satisfy both disciplines to satisfy their join.

---

## 3. Clojure Realization in `phosphene.seme.pheme`

```clojure
(require '[phosphene.seme.pheme :as pheme])

;; Construct two analytical vertices
(def v-phonology
  (pheme/vertex :vertex/phonology [:ling/id :phon/ipa :shared/gloss]))

(def v-morphosyntax
  (pheme/vertex :vertex/morphosyntax [:ling/id :syn/pos :shared/gloss]))

;; 1. Check Subsumption
(pheme/subsumes? v-phonology v-phonology) ;=> true (reflexivity)
(pheme/subsumes? v-phonology v-morphosyntax) ;=> false

;; 2. Compute Greatest Lower Bound (Meet)
(def v-consensus (pheme/vertex-meet v-phonology v-morphosyntax))
(:vertex/req v-consensus)
;;=> #{:ling/id :shared/gloss}

;; 3. Compute Least Upper Bound (Join)
(def v-composite (pheme/vertex-join v-phonology v-morphosyntax))
(:vertex/req v-composite)
;;=> #{:ling/id :phon/ipa :syn/pos :shared/gloss}
```

---

## 4. Verification & Generative Invariants

The vertex lattice is verified across 100 randomized trials in `phosphene.seme.core-test` via `clojure.spec.test.alpha`:
* `s/fdef pheme/subsumes?`: Verifies that boolean truth matches `set/subset?` over required keys.
* `s/fdef pheme/vertex-meet`: Verifies that meet required keys equal set intersection and that both parents subsume the meet.
* `s/fdef pheme/vertex-join`: Verifies that join required keys equal set union and that the join subsumes both parents.
