---
uri: seme/wiki/syntagmatic-ast-pipeline-and-python-bridge
title: Syntagmatic Markdown AST Pipeline and Python Ingress Bridge (T-SEM-012)
owner: edphos
status: living
updated: 2026-11-14
description: "Architecture, specifications, and workflows for end-to-end ingestion of on-disk Markdown AST streams, Python JSON manifests, and external verifier invocation in phosphene/seme."
---

# Syntagmatic Markdown AST Pipeline and Python Ingress Bridge (`T-SEM-012`)

`phosphene/seme` operates as the internal multi-vertex predication engine of the Phosphene platform. While `:mode-D` deductive operations run over in-memory Clojure data structures, empirical science produces on-disk artifacts: **Markdown close-reading files**, **annotated JSON manifests**, and **Python verification scripts** (such as those developed in Topic 98577 and Topic 93593).

`T-SEM-012` establishes the end-to-end bridge between on-disk sequential texts, external Python tooling, and `seme.chain` predicative grammars.

---

## 1. Architectural Topology

```
   On-Disk Corpus (Markdown / PDFs)
                 │
                 ▼
   [Python Verifier / Extractor] ──(Deterministic Invariant Audit)
                 │
                 ├──────────────────────────────┐
                 ▼                              ▼
      Annotated JSON Manifest          Markdown Block Stream
                 │                              │
                 ▼                              ▼
     [bridge/ingest-manifest]        [md/parse-markdown-blocks]
                 │                              │
                 ▼                              ▼
     Mode-D Sovereign Referent        [bridge/validate-markdown-chain]
                 │                              │
                 ▼                              ▼
    [pheme/conformed-witness]       [chain/conformed-chain-witness]
                 │                              │
                 └──────────────┬───────────────┘
                                │
                                ▼
                   Multi-Vertex Seme Proof AST
```

---

## 2. Delivered Primitives

### A. Markdown AST Parser (`phosphene.seme.markdown`)
- **`md/parse-markdown-blocks`:** Decomposes a raw markdown document into coordinate-indexed AST block records (`:coordinate`, `:block-type`, `:level`, `:raw-text`, `:tokens`).
  - Supported block types: `:frontmatter`, `:heading`, `:paragraph`, `:blockquote`, `:list-item`, `:code`.
  - Coordinates follow mdrag ID-Path parity: e.g. `doc-01:h1:1`, `doc-01:p:2`, `doc-01:quote:3`.
- **`md/tokenize-text`:** Strips non-alphanumeric noise and normalizes text into lowercased keyword tokens for sememic feature matching.
- **`md/read-markdown-file`:** Direct disk ingestion utility.

### B. Syntagmatic Sequence Validation (`phosphene.seme.bridge`)
- **`bridge/validate-markdown-chain`:** Streams parsed Markdown blocks directly into a `seme.chain/chain-grammar`.
  - Grammars can assert sequential order rules: e.g. a scientific claim must follow a heading and precede an empirical blockquote citation:
    ```clojure
    (def grammar
      (chain/chain-grammar
        :grammar/scientific-proof
        [(chain/slot :header (fn [b] (= :heading (:block-type b))))
         (chain/slot :claim  (fn [b] (= :paragraph (:block-type b))))
         (chain/slot :quote  (fn [b] (= :blockquote (:block-type b))))]))
    ```
  - Returns a proof-carrying `chain-witness` containing conformed slot bindings and failure localizations.

### C. Mode-S Manifest Rehydration (`phosphene.seme.bridge`)
- **`bridge/ingest-manifest-entries`:** Ingests unnamespaced Python/JSON manifest dictionaries and rehydrates them into sovereign Mode-D coordinates via `delome/rehydrate-unqualified`.
- **`bridge/run-python-verifier`:** Safely triggers deterministic external Python scripts (e.g. `scripts/verify_close_read.py`) via subprocess, auditing exit codes and stdout outputs without crashing the Clojure runtime.

---

## 3. Concrete Example: Ingesting a Lane Chapter 1 Close Read

```clojure
(ns example.pipeline
  (:require [phosphene.seme.core :as seme]
            [clojure.spec.alpha :as s]))

;; 1. Ingest Manifest from Python Audit
(s/def :lane.cr/id string?)
(s/def :lane.cr/unit-of-reason string?)

(def raw-entries
  [{:id "CR-LANE-004" :unit_of_reason "Information Alone Fails..."}])

(def sovereign-entries
  (seme/ingest-manifest-entries
    raw-entries
    {:id :lane.cr/id
     :unit_of_reason :lane.cr/unit-of-reason}))

;; 2. Ingest Markdown file and match Syntagmatic Chain
(def grammar
  (seme/chain-grammar
    :grammar/lane-claim
    [(seme/slot :title (fn [b] (= :heading (:block-type b))))
     (seme/slot :body  (fn [b] (= :paragraph (:block-type b))))]))

(def witness
  (seme/validate-markdown-chain
    "work/topic-98577-public-evolutionary-science/genealogy/priors/prior-005-mitchell-1961.md"
    grammar))

;; Returns conformed proof witness of the text structure
(:chain/valid? witness)
;;=> true
```
