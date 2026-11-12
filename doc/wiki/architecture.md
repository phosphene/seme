---
uri: seme/wiki/architecture
title: Triadic Architecture and Module Design of phosphene/seme
owner: edphos
status: living
updated: 2026-11-12
description: "Internal architecture, namespace boundaries, and data flow of phosphene/seme across rheme, pheme, and delome."
---

# Triadic Architecture & Module Design

`phosphene/seme` enforces strict logic/IO separation, pure in-memory execution, and Tellman naming principles across its internal namespaces.

---

## 1. Namespace Hierarchy

```
phosphene.seme
├── core    ; Public re-export surface
├── rheme   ; Firstness: Unsaturated feature predicates & componential bundles
├── pheme   ; Secondness: Perceptual judgment, conformance & multi-vertex gating
├── delome  ; Thirdness: Contextual overlays, habit inference & falsification
└── spec    ; Invariant specifications (s/fdef) and dense test.check generators
```

---

## 2. Module Responsibilities & Flow

```
+--------------------------------------------------------------------------+
| phosphene.seme.rheme (Firstness)                                         |
|   - seme: [id, pred, meta] -> Unsaturated Predicate                      |
|   - sememe: [semes] -> Distinctive Feature Bundle                        |
|   - plerematic-difference: [sememe-a, sememe-b] -> Feature Differential  |
+--------------------------------------------------------------------------+
                                    |
                                    v (Saturation over Referent)
+--------------------------------------------------------------------------+
| phosphene.seme.pheme (Secondness)                                        |
|   - conformed-witness: [spec, referent] -> Proof-Carrying Witness        |
|   - project-vertices: [vertices, referent] -> Multi-Vertex Projections   |
|   - explain-anomaly: [witness] -> Structured Machine Diagnostic          |
+--------------------------------------------------------------------------+
                                    |
                                    v (Habit & Systematic Invariance)
+--------------------------------------------------------------------------+
| phosphene.seme.delome (Thirdness)                                        |
|   - contextual-overlay: [base, overlay] -> Scoped Selectional Overlay   |
|   - infer-candidate-spec: [Mode-S Corpus] -> Inductive Candidate Spec   |
|   - boundary-specimen?: [vertex-a, vertex-b, specimen] -> Falsification  |
+--------------------------------------------------------------------------+
```

---

## 3. Tellman Naming Compliance

In accordance with our platform standard `meta/reference/clojure-naming-and-indirection-standards.md`:

1. **Pure Transformations Avoid Verbs:**
   * `plerematic-difference` (not `compute-plerematic-difference`)
   * `conformed-witness` (not `build-conformed-witness`)
   * `contextual-overlay` (not `make-contextual-overlay`)
2. **Predicates End in `?`:**
   * `seme?`, `sememe?`, `witness?`, `admits-vertex?`, `boundary-specimen?`
3. **No Implementation Leaks into Signs:**
   * Sign indicates the sense (`sememe`, `witness`), never the container (`vector-semes`, `map-witness`).
