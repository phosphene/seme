(ns phosphene.seme.markdown
  "Markdown AST Ingestion & Syntagmatic Chain Integration (T-SEM-012).
   Provides self-sufficient parsing of on-disk Markdown documents into
   structured, coordinate-indexed block streams adhered to mdrag topology.
   Enables direct parsing of linear Markdown block sequences through
   seme.chain grammars and multi-vertex proof-carrying evaluation."
  (:require [clojure.string :as str]
            [clojure.spec.alpha :as s]))

;; --- Markdown AST Specs ---

(s/def ::coordinate string?)
(s/def ::block-type #{:frontmatter :heading :paragraph :blockquote :list-item :code :unknown})
(s/def ::raw-text string?)
(s/def ::token-symbol keyword?)
(s/def ::tokens (s/coll-of ::token-symbol :kind vector?))

(s/def ::ast-block
  (s/keys :req-un [::coordinate ::block-type ::tokens ::raw-text]
          :opt-un [::level ::meta]))

;; --- Tokenizer ---

(defn tokenize-text
  "Normalizes raw string text into a vector of lowercased keyword symbols,
   stripping punctuation and non-alphanumeric noise."
  [text]
  (if (str/blank? text)
    []
    (->> (str/split (str/lower-case text) #"[^a-zA-Z0-9_\-]+")
         (remove str/blank?)
         (mapv keyword))))

;; --- Frontmatter & Block Parsing ---

(defn parse-markdown-blocks
  "Parses raw markdown string content into a vector of coordinate-indexed AST block records.
   doc-id: document identifier prefix (e.g. filename or URI)."
  [doc-id content]
  (let [lines (str/split-lines (or content ""))]
    (loop [remaining lines
           block-idx 0
           acc []
           in-frontmatter? false
           frontmatter-lines []]
      (if (empty? remaining)
        acc
        (let [line (first remaining)]
          (cond
            ;; Blank lines: skip
            (str/blank? (str/trim line))
            (recur (rest remaining) block-idx acc in-frontmatter? frontmatter-lines)

            ;; YAML Frontmatter delimiters
            (and (= (str/trim line) "---") (zero? block-idx) (empty? acc) (not in-frontmatter?))
            (recur (rest remaining) block-idx acc true [])

            (and (= (str/trim line) "---") in-frontmatter?)
            (let [fm-raw (str/join "\n" frontmatter-lines)
                  coord (str doc-id ":frontmatter")
                  block {:coordinate coord
                         :block-type :frontmatter
                         :raw-text fm-raw
                         :tokens (tokenize-text fm-raw)}]
              (recur (rest remaining) (inc block-idx) (conj acc block) false []))

            in-frontmatter?
            (recur (rest remaining) block-idx acc true (conj frontmatter-lines line))

            ;; Code block: ``` ... ```
            (str/starts-with? (str/trim line) "```")
            (let [code-lines (take-while #(not (str/starts-with? (str/trim %) "```")) (rest remaining))
                  consumed (+ 2 (count code-lines))
                  clean-code (str/join "\n" code-lines)
                  coord (str doc-id ":code:" block-idx)
                  block {:coordinate coord
                         :block-type :code
                         :raw-text clean-code
                         :tokens (tokenize-text clean-code)}]
              (recur (drop consumed remaining) (inc block-idx) (conj acc block) false []))

            ;; Heading: # ...
            (str/starts-with? (str/trim line) "#")
            (let [level (count (take-while #(= % \#) (str/trim line)))
                  clean-title (str/trim (subs (str/trim line) level))
                  coord (str doc-id ":h" level ":" block-idx)
                  block {:coordinate coord
                         :block-type :heading
                         :level level
                         :raw-text clean-title
                         :tokens (tokenize-text clean-title)}]
              (recur (rest remaining) (inc block-idx) (conj acc block) false []))

            ;; Blockquote: > ...
            (str/starts-with? (str/trim line) ">")
            (let [quote-lines (take-while #(str/starts-with? (str/trim %) ">") remaining)
                  clean-quote (->> quote-lines
                                   (map #(str/trim (str/replace-first (str/trim %) #"^>\s*" "")))
                                   (str/join " "))
                  coord (str doc-id ":quote:" block-idx)
                  block {:coordinate coord
                         :block-type :blockquote
                         :raw-text clean-quote
                         :tokens (tokenize-text clean-quote)}]
              (recur (drop (count quote-lines) remaining) (inc block-idx) (conj acc block) false []))

            ;; List item: * or -
            (re-find #"^[\*\-]\s+" (str/trim line))
            (let [clean-item (str/replace-first (str/trim line) #"^[\*\-]\s+" "")
                  coord (str doc-id ":item:" block-idx)
                  block {:coordinate coord
                         :block-type :list-item
                         :raw-text clean-item
                         :tokens (tokenize-text clean-item)}]
              (recur (rest remaining) (inc block-idx) (conj acc block) false []))

            ;; Default: Paragraph
            :else
            (let [para-lines (take-while #(and (not (str/blank? (str/trim %)))
                                               (not (str/starts-with? (str/trim %) "#"))
                                               (not (str/starts-with? (str/trim %) "```"))
                                               (not (str/starts-with? (str/trim %) ">"))
                                               (not (re-find #"^[\*\-]\s+" (str/trim %))))
                                         remaining)
                  clean-para (str/join " " (map str/trim para-lines))
                  coord (str doc-id ":p:" block-idx)
                  block {:coordinate coord
                         :block-type :paragraph
                         :raw-text clean-para
                         :tokens (tokenize-text clean-para)}]
              (recur (drop (count para-lines) remaining) (inc block-idx) (conj acc block) false []))))))))

(defn read-markdown-file
  "Reads a Markdown file from path and extracts tokenized notation blocks with coordinates."
  [path]
  (let [f (clojure.java.io/file path)
        doc-id (.getName f)
        content (slurp f)]
    (parse-markdown-blocks doc-id content)))
