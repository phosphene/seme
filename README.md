# phosphene/seme

**Multi-Vertex Predication, Distinctive Semantic Features, and Proof-Carrying Conformance over Open Referents in Clojure.**

`phosphene/seme` is a pure `:mode-D` deductive engine within the Phosphene platform architecture. It completes the foundational semiotic triad alongside `phosphene/syntagma` and `phosphene/paradigma`:

$$\begin{aligned}
\text{\textbf{phosphene/syntagma}} &\quad\longrightarrow\quad \text{Horizontal axis of concatenation and linear ordering } (\sigma_1 \prec \sigma_2 \prec \sigma_3) \\
\text{\textbf{phosphene/paradigma}} &\quad\longrightarrow\quad \text{Vertical axis of substitution and equivalence quotienting } (t \mapsto [t]_\sim) \\
\text{\textbf{phosphene/seme}} &\quad\longrightarrow\quad \text{Internal axis of predication and distinctive semantic features } (x \models P)
\end{aligned}$$

---

## 1. Philosophical & Linguistic Genealogy

`phosphene/seme` synthesizes two foundational traditions that resolve Rich Hickey’s critique of nominal type hierarchies:

1. **C.S. Peirce's 1906 Triad (*Prolegomena to an Apology for Pragmaticism*, CP 4.538):**
   - **Seme / Rheme (Firstness):** An unsaturated qualitative sign or affordance (`"___ is telic"`, `"___ burns"`) prior to propositional assertion.
   - **Pheme / Dicisign (Secondness):** An indexical judgment or proposition asserting a perceptual fact about a referent.
   - **Delome / Argument (Thirdness):** An inferential habit or rule governing systematic transitions.

2. **European Structuralist Componential Semantics (Buyssens, Pottier, Greimas, Coseriu):**
   - A **seme** is the minimal distinctive semantic feature (e.g. `[+animate]`, `[+human]`, `[-adult]`).
   - A bundle of distinctive semes forms a **sememe** (the content plane of a linguistic sign).

In Clojure, each registered namespaced keyword (`s/def :seme.feat/telic boolean?`) operates simultaneously as an independent semantic feature within an open map bundle (`s/keys`) and an unsaturated Peircean predicate awaiting referent conformance (`s/conform`).

---

## 2. Architecture & Module Structure

The library is organized around Peirce's 1906 trichotomy:

| Namespace | Peircean Category | Domain Focus | Key Functions |
| :--- | :--- | :--- | :--- |
| **`phosphene.seme.rheme`** | **Firstness** | Unsaturated predicates & componential feature bundles | `seme`, `sememe`, `bundle-semes`, `plerematic-difference` |
| **`phosphene.seme.pheme`** | **Secondness** | Perceptual judgment, conformance & multi-vertex gating | `conformed-witness`, `admits-vertex?`, `project-vertices`, `explain-anomaly` |
| **`phosphene.seme.delome`** | **Thirdness** | Inferential habits, selectional restrictions & falsification | `contextual-overlay`, `evaluate-overlay`, `infer-candidate-spec`, `boundary-specimen?` |
| **`phosphene.seme.spec`** | **Invariants** | Formal specifications and dense `test.check` property generators | `s/fdef`, `::seme`, `::sememe`, `::witness` |

---

## 3. Seven Architectural Predications (SEME-ARCH)

`phosphene/seme` implements seven core architectural predications:

1. **Sovereign Intension (`SEME-ARCH-001`):** Attribute meaning is bound to globally namespaced keywords, decoupling meaning from host record types.
2. **Open-Vertex Projection (`SEME-ARCH-002`):** Orthogonal analytical vertices (phonological, morphosyntactic, historical) project onto a shared referent map without inheritance or key erasure.
3. **Proof-Carrying Parse (`SEME-ARCH-003`):** Conformance produces a structured witness parse tree (`:witness/parsed`) paired with the symbolic predicate anatomy (`:witness/anatomy`) instead of an opaque boolean.
4. **Componential Algebra (`SEME-ARCH-004`):** Distinctive semantic features combine, intersect, and diff (`plerematic-difference`) as pure data sets.
5. **Scoped Vertex Overlays (`SEME-ARCH-005`):** Contextual selectional restrictions layer over sovereign coordinates without mutating global definitions.
6. **Bifunctorial Bridge (`SEME-ARCH-006`):** Deductive specs (`Mode-D`) compile into inspectable schemas, while inductive Mode-S observation corpora distill into candidate specs (`infer-candidate-spec`).
7. **Differential Falsification (`SEME-ARCH-007`):** Generative boundary testing synthesizes specimens that satisfy one analytical vertex while falsifying another (`boundary-specimen?`).

---

## 4. Usage Example

```clojure
(require '[clojure.spec.alpha :as s]
         '[phosphene.seme.core :as seme])

;; Define sovereign attributes
(s/def :ling/id string?)
(s/def :phon/ipa string?)
(s/def :syn/pos #{:noun :verb})

;; Define orthogonal analytical vertices
(s/def :vertex/phonology (s/keys :req [:ling/id :phon/ipa]))
(s/def :vertex/morphosyntax (s/keys :req [:ling/id :syn/pos]))

;; Open-world referent containing multi-vertex data
(def referent
  {:ling/id "entry-101"
   :phon/ipa "/kæt/"
   :syn/pos :noun
   :extra/historical-note "Attested in Old English"})

;; Project vertices onto the referent
(def projections
  (seme/project-vertices
    {:phonology :vertex/phonology
     :syntax :vertex/morphosyntax}
    referent))

(get-in projections [:phonology :witness/valid?]) ;=> true
(get-in projections [:syntax :witness/valid?])    ;=> true
```

---

## 5. Verification & Testing

Run unit and generative property checks:

```bash
clojure -M:runner
```

---

## License

MIT License. Copyright (c) 2026 Phosphene Platform Team.
