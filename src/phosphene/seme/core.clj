(ns phosphene.seme.core
  "Seme: Multi-vertex predication, distinctive semantic features,
   and proof-carrying conformance over open referents.
   Root namespace re-exporting fundamental abstractions across:
     - phosphene.seme.rheme  (Firstness:  Unsaturated feature predicates & bundles)
     - phosphene.seme.pheme  (Secondness: Perceptual judgment, conformance, multi-vertex gating & lattice)
     - phosphene.seme.delome (Thirdness:  Selectional restrictions, habit inference & falsification)
     - phosphene.seme.chain  (Grammar:    Sequential predicative grammars & syntagma interop)"
  (:require [phosphene.seme.rheme :as rheme]
            [phosphene.seme.pheme :as pheme]
            [phosphene.seme.delome :as delome]
            [phosphene.seme.chain :as chain]
            [phosphene.seme.complementarity :as comp]
            [phosphene.seme.markdown :as md]
            [phosphene.seme.bridge :as bridge]
            [phosphene.seme.actant :as actant]))

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
(def retrograde-ancestor-trace pheme/retrograde-ancestor-trace)
(def retract-vertex pheme/retract-vertex)
(def lattice-provocation pheme/lattice-provocation)
(def explain-anomaly pheme/explain-anomaly)

;; --- Re-exported Delome Primitives (Thirdness) ---

(def contextual-overlay delome/contextual-overlay)
(def evaluate-overlay delome/evaluate-overlay)
(def infer-candidate-spec delome/infer-candidate-spec)
(def rehydrate-unqualified delome/rehydrate-unqualified)
(def disentangle-provocation-tier delome/disentangle-provocation-tier)
(def customary-transition delome/customary-transition)
(def contingent-prediction delome/contingent-prediction)
(def epistemic-cycle delome/epistemic-cycle)
(def pragmatic-conditional delome/pragmatic-conditional)
(def boundary-specimen? delome/boundary-specimen?)
(def synthesize-boundary-specimen delome/synthesize-boundary-specimen)

;; --- Re-exported Chain Primitives (Syntagmatic Predicative Grammars) ---

(def slot chain/slot)
(def alt-slot chain/alt-slot)
(def nested-slot chain/nested-slot)
(def slot? chain/slot?)
(def chain-grammar chain/chain-grammar)
(def chain-grammar? chain/chain-grammar?)
(def conformed-chain-witness chain/conformed-chain-witness)
(def validate-chain chain/validate-chain)
(def grammar-commutator chain/grammar-commutator)

;; --- Re-exported Complementarity Primitives (Non-Commutative Epistemology, T-SEM-013) ---

(def complementary-dyad comp/complementary-dyad)
(def complementary-dyad? comp/complementary-dyad?)
(def commutator comp/commutator)
(def assert-non-collapsible! comp/assert-non-collapsible!)
(def extract-hinge-namespace comp/extract-hinge-namespace)
(def sovereign-hinges-distinct? comp/sovereign-hinges-distinct?)
(def incommensurability-dossier comp/incommensurability-dossier)
(def dual-aspect-witness comp/dual-aspect-witness)

;; --- Re-exported Markdown & Python Interop Primitives (T-SEM-012) ---

(def parse-markdown-blocks md/parse-markdown-blocks)
(def read-markdown-file md/read-markdown-file)
(def tokenize-text md/tokenize-text)
(def ingest-manifest-entries bridge/ingest-manifest-entries)
(def validate-markdown-chain bridge/validate-markdown-chain)
(def run-python-verifier bridge/run-python-verifier)

;; --- Re-exported Actantial Syntax & Narrative Programs (Greimas) ---

(def actantial-model actant/actantial-model)
(def actantial-model? actant/actantial-model?)
(def get-actant actant/get-actant)
(def actors-for-role actant/actors-for-role)
(def find-roles-for-actor actant/find-roles-for-actor)
(def narrative-state actant/narrative-state)
(def narrative-state? actant/narrative-state?)
(def conjoined? actant/conjoined?)
(def disjoined? actant/disjoined?)
(def narrative-program actant/narrative-program)
(def narrative-program? actant/narrative-program?)
(def execute-program actant/execute-program)
(def canonical-trajectory actant/canonical-trajectory)
