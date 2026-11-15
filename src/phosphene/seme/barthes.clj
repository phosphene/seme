(ns phosphene.seme.barthes
  "Roland Barthes' Structural Analysis of Narratives & S/Z Plural Textuality.
   Formalizes:
     - Lexias: Coordinate-preserving, variable-width textual segments (S/Z §VI)
     - Distributional Units (Functions):
         * Cardinal Functions / Nuclei (noyaux): load-bearing hinge points opening/closing narrative branches
         * Catalyzers (catalyses): interstitial descriptive/pacing expansions between nuclei
     - Integrational Units (Indices):
         * Indices Proper: implicit semantic feature clusters (character, atmosphere, philosophy)
         * Informants: explicit spatio-temporal and empirical localizers
     - The Five-Code Braid (structuration across S/Z §XI-XII):
         * :code/proairetic  (actions, empirical sequences, narrative programs)
         * :code/hermeneutic (enigmas, delays, questions, disclosures)
         * :code/semic       (qualitative semes, thematic constellations)
         * :code/symbolic    (antitheses, transgressions, semiotic square tensions)
         * :code/cultural    (gnomic, scientific, historical reference codes)
     - extract-code-stream: projects braided lexia sequence along a single code plane."
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [clojure.spec.alpha :as s]
            [phosphene.seme.actant :as actant]
            [phosphene.seme.markdown :as md]))

;; --- The Canonical Five Codes of S/Z ---

(def five-codes
  "The five canonical semiotic codes defined by Roland Barthes in S/Z (1970)."
  #{:code/proairetic :code/hermeneutic :code/semic :code/symbolic :code/cultural})

(def narrative-unit-types
  "The four classes of narrative units from Barthes' 1966 'Introduction to the Structural Analysis of Narratives'."
  #{:function/nucleus :function/catalyzer :index/semantic :index/informant})

;; --- 1. Lexia Segmentation ---

(defn make-lexia
  "Constructs a formal Barthesian Lexia record.
   - id: Unique lexia identifier (string or keyword).
   - coordinate: Parent coordinate (Mdrag ID-Path coordinate, e.g. 'doc.md:p:1#L0').
   - tokens: Vector of token keywords.
   - raw-text: String representation of the lexia text.
   - unit-type: One of #{:function/nucleus :function/catalyzer :index/semantic :index/informant}.
   - codes: Map of five codes {:code/proairetic [...] ...}
   - metadata: Optional metadata map."
  ([id coordinate tokens raw-text]
   (make-lexia id coordinate tokens raw-text :function/catalyzer {} {}))
  ([id coordinate tokens raw-text unit-type codes]
   (make-lexia id coordinate tokens raw-text unit-type codes {}))
  ([id coordinate tokens raw-text unit-type codes metadata]
   {:lexia/id (if (keyword? id) id (keyword (str id)))
    :lexia/coordinate coordinate
    :lexia/tokens (vec (or tokens []))
    :lexia/raw-text (or raw-text "")
    :lexia/unit-type (or unit-type :function/catalyzer)
    :lexia/codes (into {} (map (fn [c] [c (vec (get codes c []))]) five-codes))
    :lexia/metadata metadata}))

(defn lexia?
  "Returns true if x is a valid Barthesian lexia record."
  [x]
  (and (map? x)
       (keyword? (:lexia/id x))
       (string? (:lexia/coordinate x))
       (vector? (:lexia/tokens x))
       (contains? narrative-unit-types (:lexia/unit-type x))
       (map? (:lexia/codes x))))

(defn- split-sentence-spans
  "Splits raw text and token list into contiguous sentence/clause spans.
   Uses punctuation boundaries (. ; : ? !) while preserving tokens."
  [raw-text tokens]
  (let [sentences (->> (str/split (or raw-text "") #"(?<=[.;:?!])\s+")
                       (map str/trim)
                       (remove str/blank?)
                       vec)]
    (if (or (empty? sentences) (<= (count sentences) 1))
      [{:text raw-text :tokens tokens}]
      ;; Partition tokens matching word stems in each sentence
      (loop [remaining-sentences sentences
             remaining-tokens tokens
             acc []]
        (if (empty? remaining-sentences)
          (if (seq remaining-tokens)
            (update acc (dec (count acc)) (fn [last-span]
                                            (update last-span :tokens into remaining-tokens)))
            acc)
          (let [s (first remaining-sentences)
                s-tokens (md/tokenize-text s)
                match-cnt (count s-tokens)
                taken-tokens (subvec remaining-tokens 0 (min match-cnt (count remaining-tokens)))
                rest-tokens (subvec remaining-tokens (min match-cnt (count remaining-tokens)))]
            (recur (rest remaining-sentences)
                   rest-tokens
                   (conj acc {:text s :tokens taken-tokens}))))))))

(defn segment-lexias
  "Segments Markdown AST blocks (from phosphene.seme.markdown/parse-markdown-blocks)
   into coordinate-preserving, contiguous Lexia records.
   opts:
     :granularity - :block (1 block = 1 lexia) or :clause (split by sentence boundaries, default)
     :unit-type-fn - (fn [tokens raw-text block-type] unit-type)
   Preserves strict token conservation and Mdrag :coordinate lineage."
  ([blocks]
   (segment-lexias blocks {}))
  ([blocks opts]
   (let [granularity (get opts :granularity :clause)
         unit-fn (get opts :unit-type-fn (constantly :function/catalyzer))]
     (vec
       (mapcat
         (fn [block]
           (let [coord (:coordinate block)
                 b-type (:block-type block)
                 raw-txt (:raw-text block)
                 b-tokens (:tokens block)]
             (if (or (= granularity :block) (empty? b-tokens))
               [(make-lexia (str coord "#L0")
                            coord
                            b-tokens
                            raw-txt
                            (unit-fn b-tokens raw-txt b-type)
                            {}
                            {:source/block-type b-type})]
               ;; Split by sentence/clause boundaries
               (let [spans (split-sentence-spans raw-txt b-tokens)]
                 (map-indexed
                   (fn [idx span]
                     (let [l-id (str coord "#L" idx)]
                       (make-lexia l-id
                                    coord
                                    (:tokens span)
                                    (:text span)
                                    (unit-fn (:tokens span) (:text span) b-type)
                                    {}
                                    {:source/block-type b-type
                                     :lexia/index idx})))
                   spans)))))
         blocks)))))

;; --- 2. Narrative Unit Classification (Barthes 1966) ---

(defn classify-narrative-unit
  "Classifies a lexia into one of the 4 structural narrative unit types:
   1. :function/nucleus - Cardinal function opening or closing a causal alternative
      (e.g. executes an actantial narrative-program state change).
   2. :function/catalyzer - Interstitial expansion between nuclei (pacing, detail).
   3. :index/semantic - Implicit semantic predicate cluster pointing to theme or character.
   4. :index/informant - Explicit spatio-temporal, empirical, or chronological setting."
  ([lexia]
   (classify-narrative-unit lexia {}))
  ([lexia opts]
   (let [tokens (set (:lexia/tokens lexia))
         codes (:lexia/codes lexia)
         pro-acts (get codes :code/proairetic [])
         sem-acts (get codes :code/semic [])
         has-program? (or (some #(and (map? %) (contains? % :pn/id)) pro-acts)
                          (contains? tokens :then)
                          (contains? tokens :therefore)
                          (contains? tokens :decided)
                          (contains? tokens :transformed)
                          (get opts :nucleus? false))
         has-informant? (or (some #(contains? #{:year :date :place :at :in :during :century} %) tokens)
                            (get opts :informant? false))
         has-indices? (or (seq sem-acts)
                          (some #(contains? #{:dark :light :melancholy :fury :noble :cold} %) tokens)
                          (get opts :index? false))]
     (cond
       has-program? :function/nucleus
       has-informant? :index/informant
       has-indices? :index/semantic
       :else :function/catalyzer))))

;; --- 3. The Five-Code Braid (structuration) ---

(defn attach-codes
  "Attaches or merges semiotic code annotations into an existing lexia."
  [lexia code-map]
  (let [existing-codes (:lexia/codes lexia)
        merged-codes (reduce
                       (fn [acc [k v]]
                         (if (contains? five-codes k)
                           (let [items (cond
                                         (nil? v) []
                                         (vector? v) v
                                         (sequential? v) (vec v)
                                         :else [v])]
                             (update acc k (fnil into []) items))
                           acc))
                       existing-codes
                       code-map)]
    (assoc lexia :lexia/codes merged-codes)))

(defn braid-codes
  "Annotates a sequence of lexias across the five Barthesian semiotic codes:
     - :code/proairetic  (Action sequences, deliberate choices, narrative programs)
     - :code/hermeneutic (Enigmas, promises, delays, snares, partial answers, disclosures)
     - :code/semic       (Qualitative semes, thematic constellations)
     - :code/symbolic    (Binary antitheses, sacred/taboo bars, semiotic square oppositions)
     - :code/cultural    (Gnomisms, scientific/historical reference codes)
   coder-fn: (fn [lexia] code-map) returning a map with any subset of the five codes.
   Also recalculates :lexia/unit-type based on the assigned codes."
  ([lexias]
   (braid-codes lexias (constantly {})))
  ([lexias coder-fn]
   (mapv
     (fn [lexia]
       (let [code-annotations (coder-fn lexia)
             braided (attach-codes lexia code-annotations)
             unit-type (if (= :function/catalyzer (:lexia/unit-type braided))
                         (classify-narrative-unit braided)
                         (:lexia/unit-type braided))]
         (assoc braided :lexia/unit-type unit-type)))
     lexias)))

(defn extract-code-stream
  "Projects a braided sequence of lexias along a single semiotic code axis.
   Returns a vector of items containing:
     {:lexia/id ...
      :lexia/coordinate ...
      :code code-key
      :values [...]
      :tokens [...]
      :unit-type ...}
   Omits lexias where the specified code has empty annotations unless keep-empty? is true."
  ([lexias code-key]
   (extract-code-stream lexias code-key false))
  ([lexias code-key keep-empty?]
   (vec
     (keep
       (fn [lexia]
         (let [vals (get-in lexia [:lexia/codes code-key] [])]
           (when (or keep-empty? (seq vals))
             {:lexia/id (:lexia/id lexia)
              :lexia/coordinate (:lexia/coordinate lexia)
              :code code-key
              :values vals
              :tokens (:lexia/tokens lexia)
              :unit-type (:lexia/unit-type lexia)})))
       lexias))))

;; --- 4. Braid Analysis & Polyphonic Density ---

(defn polyphonic-density
  "Measures the polyphonic depth of a lexia: the number of distinct semiotic codes
   active simultaneously within it [0, 5]."
  [lexia]
  (count (filter (fn [[_ v]] (seq v)) (:lexia/codes lexia))))

(defn braid-summary
  "Computes an aggregate summary of semiotic code activity across a braided lexia sequence."
  [lexias]
  (let [total-lexias (count lexias)
        code-counts (reduce
                      (fn [acc c]
                        (assoc acc c (count (filter #(seq (get-in % [:lexia/codes c])) lexias))))
                      {}
                      five-codes)
        unit-counts (frequencies (map :lexia/unit-type lexias))]
    {:summary/total-lexias total-lexias
     :summary/code-frequencies code-counts
     :summary/unit-frequencies unit-counts
     :summary/mean-polyphony (if (zero? total-lexias)
                               0.0
                               (double (/ (reduce + (map polyphonic-density lexias)) total-lexias)))}))
