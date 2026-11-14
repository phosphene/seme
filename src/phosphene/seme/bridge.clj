(ns phosphene.seme.bridge
  "Annotated Manifest Ingress, Python Interop, and Pipeline Verification (T-SEM-012).
   Provides tools to:
     - Ingest structured JSON/YAML manifests produced by Python research scripts
     - Project parsed Markdown AST blocks and manifest entries into multi-vertex referents
     - Execute deterministic external python verification scripts via clojure.java.shell."
  (:require [clojure.string :as str]
            [clojure.java.shell :as sh]
            [clojure.spec.alpha :as s]
            [phosphene.seme.markdown :as md]
            [phosphene.seme.chain :as chain]
            [phosphene.seme.pheme :as pheme]
            [phosphene.seme.delome :as delome]))

;; --- Minimal Hermetic JSON Parser (No External Dependencies) ---

(defn parse-json-val [s]
  (let [trimmed (str/trim s)]
    (cond
      (= trimmed "true") true
      (= trimmed "false") false
      (= trimmed "null") nil
      (re-matches #"^-?\d+$" trimmed) (Long/parseLong trimmed)
      (re-matches #"^-?\d+\.\d+$" trimmed) (Double/parseDouble trimmed)
      (and (str/starts-with? trimmed "\"") (str/ends-with? trimmed "\""))
      (subs trimmed 1 (dec (count trimmed)))
      :else trimmed)))

(defn parse-simple-json-map
  "Lightweight parser for flat or simple single-level JSON dictionary strings,
   sufficient for manifest entries without bringing in heavy Maven JSON dependencies."
  [s]
  (let [trimmed (str/trim s)]
    (if (and (str/starts-with? trimmed "{") (str/ends-with? trimmed "}"))
      (let [inner (subs trimmed 1 (dec (count trimmed)))
            pairs (str/split inner #",\s*(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")]
        (reduce (fn [acc pair]
                  (let [[k-part v-part] (str/split pair #":\s*" 2)]
                    (if (and k-part v-part)
                      (let [k (keyword (str/replace (str/trim k-part) #"^\"|\"$" ""))
                            v (parse-json-val v-part)]
                        (assoc acc k v))
                      acc)))
                {}
                (remove str/blank? pairs)))
      {})))

;; --- Manifest Reading & Rehydration ---

(defn ingest-manifest-entries
  "Ingests a sequence of raw unnamespaced manifest maps (e.g. from Python output),
   rehydrating each entry into sovereign Mode-D coordinates via delome/rehydrate-unqualified.
   entries: collection of unqualified maps
   coord-alias-map-or-ns: map of {unqualified-key sovereign-namespaced-key} or namespace prefix keyword.
   opts: optional options forwarded to delome/rehydrate-unqualified (e.g. :validate?)"
  ([entries coord-alias-map-or-ns]
   (ingest-manifest-entries entries coord-alias-map-or-ns {}))
  ([entries coord-alias-map-or-ns opts]
   (mapv (fn [entry]
           (delome/rehydrate-unqualified entry coord-alias-map-or-ns opts))
         entries)))

;; --- Syntagmatic Markdown AST Pipeline ---

(defn validate-markdown-chain
  "Parses on-disk Markdown AST blocks through a sequential chain grammar.
   path: path to Markdown file on disk
   grammar: chain-grammar record (from seme.chain)
   block-filter: optional predicate filter on AST blocks (default identity).
   Returns a conformed-chain-witness of the matched sequence."
  ([path grammar]
   (validate-markdown-chain path grammar identity))
  ([path grammar block-filter]
   (let [blocks (md/read-markdown-file path)
         selected-blocks (filterv block-filter blocks)
         witness (chain/conformed-chain-witness grammar selected-blocks)]
     (assoc witness :chain/path path :chain/total-blocks (count blocks)))))

;; --- Python Interop: Deterministic Script Invocation ---

(defn run-python-verifier
  "Invokes an external Python verification script via subprocess shell.
   Options can specify :dir (working directory).
   script-path: path to python script (e.g. scripts/verify_close_read.py)
   args: collection of arguments (e.g. manifest paths)
   Returns a map:
     {:verifier/success? boolean
      :verifier/exit-code int
      :verifier/stdout string
      :verifier/stderr string}."
  [& args]
  (let [[opts rest-args] (if (map? (first args))
                           [(first args) (rest args)]
                           [{} args])
        script-path (first rest-args)
        cmd-args (rest rest-args)
        cmd (into ["python3" script-path] cmd-args)
        sh-opts (filterv some? (flatten (seq (cond-> []
                                               (:dir opts) (into [:dir (:dir opts)])))))]
    (try
      (let [res (apply sh/sh (concat cmd sh-opts))]
        {:verifier/success? (zero? (:exit res))
         :verifier/exit-code (:exit res)
         :verifier/stdout (:out res)
         :verifier/stderr (:err res)})
      (catch java.io.IOException e
        {:verifier/success? false
         :verifier/exit-code -1
         :verifier/stdout ""
         :verifier/stderr (.getMessage e)
         :verifier/error-type :io-exception}))))
