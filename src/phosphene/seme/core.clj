(ns phosphene.seme.core
  "Seme: Multi-vertex predication, distinctive semantic features,
   and proof-carrying conformance over open referents.
   Root namespace re-exporting fundamental abstractions across:
     - phosphene.seme.rheme  (Firstness:  Unsaturated feature predicates & bundles)
     - phosphene.seme.pheme  (Secondness: Perceptual judgment, conformance, multi-vertex gating & lattice)
     - phosphene.seme.delome (Thirdness:  Selectional restrictions, habit inference & falsification)"
  (:require [phosphene.seme.rheme :as rheme]
            [phosphene.seme.pheme :as pheme]
            [phosphene.seme.delome :as delome]))

;; --- Re-exported Rheme Primitives (Firstness) ---

(def seme rheme/seme)
(def sememe rheme/sememe)
(def bundle-semes rheme/bundle-semes)
(def plerematic-difference rheme/plerematic-difference)

;; --- Re-exported Pheme Primitives (Secondness) ---

(def conformed-witness pheme/conformed-witness)
(def vertex pheme/vertex)
(def vertex? pheme/vertex?)
(def admits-vertex? pheme/admits-vertex?)
(def project-vertices pheme/project-vertices)
(def subsumes? pheme/subsumes?)
(def vertex-meet pheme/vertex-meet)
(def vertex-join pheme/vertex-join)
(def derivation-chain-witness pheme/derivation-chain-witness)
(def explain-anomaly pheme/explain-anomaly)

;; --- Re-exported Delome Primitives (Thirdness) ---

(def contextual-overlay delome/contextual-overlay)
(def evaluate-overlay delome/evaluate-overlay)
(def infer-candidate-spec delome/infer-candidate-spec)
(def rehydrate-unqualified delome/rehydrate-unqualified)
(def boundary-specimen? delome/boundary-specimen?)
(def synthesize-boundary-specimen delome/synthesize-boundary-specimen)
