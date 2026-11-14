# phosphene/seme

**Multi-Vertex Predication, Distinctive Semantic Features, and Proof-Carrying Conformance over Open Referents in Clojure.**

`phosphene/seme` is a pure `:mode-D` deductive engine within the Phosphene platform architecture. It completes the foundational semiotic triad alongside `phosphene/syntagma` and `phosphene/paradigma`:

$$\begin{aligned}
\text{\textbf{phosphene/syntagma}} &\quad\longrightarrow\quad \text{Horizontal axis of concatenation and linear ordering } (\sigma_1 \prec \sigma_2 \prec \sigma_3) \\
\text{\textbf{phosphene/paradigma}} &\quad\longrightarrow\quad \text{Vertical axis of substitution and equivalence quotienting } (t \mapsto [t]_\sim) \\
\text{\textbf{phosphene/seme}} &\quad\longrightarrow\quad \text{Internal axis of predication and distinctive semantic features } (x \models P)
\end{aligned}$$

Detailed philosophical genealogy and module architecture are documented in the [Hermetic Repository Wiki](doc/wiki/INDEX.md).

---

## 1. Philosophical & Linguistic Genealogy

`phosphene/seme` synthesizes three foundational traditions that resolve Rich Hickey’s critique of nominal type hierarchies:

1. **C.S. Peirce's 1906 Triad (*Prolegomena to an Apology for Pragmaticism*, CP 4.538):**
   - **Seme / Rheme (Firstness):** An unsaturated qualitative sign or affordance (`"___ is telic"`, `"___ burns"`) prior to propositional assertion.
   - **Pheme / Dicisign (Secondness):** An indexical judgment or proposition asserting a perceptual fact about a referent.
   - **Delome / Argument (Thirdness):** An inferential habit or rule governing systematic transitions.

2. **European Structuralist Componential Semantics (Buyssens, Pottier, Greimas, Coseriu):**
   - A **seme** is the minimal distinctive semantic feature (e.g. `[+animate]`, `[+human]`, `[-adult]`).
   - A bundle of distinctive semes forms a **sememe** (the content plane of a linguistic sign).

3. **Algirdas Julien Greimas (*Sémantique structurale*, 1966; *Du Sens*, 1970):**
   - **Actantial Syntax:** De-complects characters into 6 structural actant positions across 3 orthogonal axes (Desire, Transmission, Power).
   - **Narrative Programs:** Elementary transformations of state junctions ($S \wedge O \to S \vee O$ or $S \vee O \to S \wedge O$).

See [Genealogy & Semiotic Foundations](doc/wiki/genealogy.md) for the complete theoretical derivation.

---

## 2. Architecture & Module Structure

The library is organized around Peirce's 1906 trichotomy and extended with predicative regex grammars, Bohrian complementarity, Markdown AST pipelines, and Greimassian actantial syntax:

| Namespace | Semiometric Category | Domain Focus | Key Functions |
| :--- | :--- | :--- | :--- |
| **`phosphene.seme.rheme`** | **Firstness** | Unsaturated predicates & componential feature bundles | `seme`, `sememe`, `bundle-semes`, `plerematic-difference` |
| **`phosphene.seme.pheme`** | **Secondness** | Perceptual judgment, conformance, multi-vertex gating & vertex lattice | `conformed-witness`, `admits-vertex?`, `project-vertices`, `vertex-meet`, `vertex-join`, `retract-vertex` |
| **`phosphene.seme.delome`** | **Thirdness** | Inferential habits, selectional restrictions & falsification | `contextual-overlay`, `evaluate-overlay`, `infer-candidate-spec`, `boundary-specimen?`, `epistemic-cycle` |
| **`phosphene.seme.chain`** | **Syntagmatic Grammars** | Predicative regex grammars over token sequences | `chain-grammar`, `slot`, `alt-slot`, `nested-slot`, `grammar-commutator`, `conformed-chain-witness` |
| **`phosphene.seme.actant`** | **Narrative Syntax** | Greimassian actants, junctions & state transformations | `actantial-model`, `find-roles-for-actor`, `narrative-state`, `narrative-program`, `execute-program`, `canonical-trajectory` |
| **`phosphene.seme.complementarity`** | **Quantum Semiotics** | Bohrian non-commutative complementarity & hinge sovereignty | `complementary-dyad`, `commutator`, `assert-non-collapsible!`, `incommensurability-dossier` |
| **`phosphene.seme.markdown`** | **AST Ingress** | Coordinate-preserving Markdown block parser | `parse-markdown-blocks`, `read-markdown-file`, `tokenize-text` |
| **`phosphene.seme.bridge`** | **Polyglot Interop** | Python JSON manifest ingress & external verifier runners | `ingest-manifest-entries`, `validate-markdown-chain`, `run-python-verifier` |
| **`phosphene.seme.spec`** | **Invariants** | Formal specifications and dense `test.check` property generators | `s/fdef`, `::seme`, `::sememe`, `::witness`, `::chain-grammar`, `::actantial-model` |

See [Triadic Architecture & Module Design](doc/wiki/architecture.md) for data flow and Tellman naming compliance.

---

## 3. Seven Architectural Predications (`SEME-ARCH`)

`phosphene/seme` implements seven core architectural predications hyperlinked directly to their formal specifications and logic formulas in the hermetic wiki:

1. [**SEME-ARCH-001: Sovereign Intension**](doc/wiki/predications.md#seme-arch-001) — Attribute meaning is bound to globally namespaced keywords, decoupling meaning from host record types.
2. [**SEME-ARCH-002: Open-Vertex Projection**](doc/wiki/predications.md#seme-arch-002) — Orthogonal analytical vertices (phonological, morphosyntactic, historical) project onto a shared referent map without inheritance or key erasure.
3. [**SEME-ARCH-003: Proof-Carrying Parse**](doc/wiki/predications.md#seme-arch-003) — Conformance produces a structured witness parse tree (`:witness/parsed`) paired with the symbolic predicate anatomy (`:witness/anatomy`) instead of an opaque boolean.
4. [**SEME-ARCH-004: Homoiconic Componential Algebra**](doc/wiki/predications.md#seme-arch-004) — Distinctive semantic features combine, intersect, and diff (`plerematic-difference`) as pure data sets.
5. [**SEME-ARCH-005: Scoped Vertex Overlays**](doc/wiki/predications.md#seme-arch-005) — Contextual selectional restrictions layer over sovereign coordinates without mutating global definitions.
6. [**SEME-ARCH-006: Bifunctorial Bridge**](doc/wiki/predications.md#seme-arch-006) — Deductive specs (`Mode-D`) compile into inspectable schemas, while inductive Mode-S observation corpora distill into candidate specs (`infer-candidate-spec`).
7. [**SEME-ARCH-007: Differential Falsification**](doc/wiki/predications.md#seme-arch-007) — Generative boundary testing synthesizes specimens that satisfy one analytical vertex while falsifying another (`boundary-specimen?`).

For formal first-order logic axioms and Clojure s-expressions, inspect the [Architectural Predications Registry](doc/wiki/predications.md).

---

## 4. Projection of `clojure.spec.alpha` Master Coordinates (`CLJ-*`)

`phosphene/seme` formalizes the core principles of Rich Hickey's intensional design into an executable computational ontology. The complete projection and philosophical derivation of the canonical master coordinates—including Keyset Membership (`CLJ-002`), Global Sovereignty (`CLJ-003`, `CLJ-015`), Open-World Accretion (`CLJ-004`), Conformance Witnessing (`CLJ-006`), Alt-Branching Priority (`CLJ-007-ALT`), Scoped Hierarchical Nesting (`CLJ-007-NEST`), Generative Falsification (`CLJ-009`, `CLJ-012`), and Fregean Indirection (`CLJ-014`, `CLJ-016`)—is codified in [Projection of clojure.spec.alpha Master Coordinates](doc/wiki/clj-spec-projection.md).

---

## 5. Usage Examples

### Multi-Vertex Projection & Conformance
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

### Predicative Regex Grammars with Labeled Alternatives & Nesting
```clojure
(require '[phosphene.seme.chain :as chain])

(def discourse-grammar
  (chain/chain-grammar
    :grammar/discourse
    [(chain/slot :claim (s/and string? #(str/starts-with? % "Claim:")))
     (chain/alt-slot :evidence
       [[:exact-quote (s/and string? #(str/starts-with? % ">"))]
        [:code-block  (s/and string? #(str/starts-with? % "`"))]
        [:fallback    string?]]
       :plus)
     (chain/slot :conclusion (s/and string? #(str/starts-with? % "Therefore,")))]))

(def parse (chain/conformed-chain-witness discourse-grammar tokens))
(:chain/valid? parse) ;=> true
```

### Greimassian Actantial Models & Narrative Programs
```clojure
(require '[phosphene.seme.actant :as actant])

(def model (actant/actantial-model
             {:subject  :actor/hero
              :object   :actor/grail
              :sender   :actor/king
              :receiver :actor/realm
              :helper   [:actor/merlin :actor/sword]
              :opponent [:actor/dragon]}))

(actant/find-roles-for-actor model :actor/merlin)
;;=> #{:helper}

;; Narrative State Transitions
(def initial-lack (actant/narrative-state :actor/hero :disjunction :actor/grail))
(def final-gain   (actant/narrative-state :actor/hero :conjunction :actor/grail))
(def quest-pn     (actant/narrative-program :pn/grail-quest :actor/hero initial-lack final-gain))

(actant/execute-program quest-pn initial-lack)
;;=> {:status :success, :pn/id :pn/grail-quest, :state/current ...}
```

---

## 6. Verification & Testing

Run the full unit and generative property test suites across all modules:

```bash
clojure -M:runner
```

---

## License

MIT License. Copyright (c) 2026 Phosphene Platform Team.
