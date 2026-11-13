(ns phosphene.seme.delome
  "Peirce's Thirdness: Inferential habits, selectional restrictions,
   contextual overlays, inductive spec distillation, automated
   differential falsification, and unqualified ingress boundary defense (CLJ-002-U, TELL-1002).
   Bridges top-down deductive specifications with bottom-up empirical Mode-S data."
  (:require [clojure.set :as set]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.test.check.generators :as tc-gen]
            [clojure.test.check.rose-tree :as rose]))

;; --- Contextual Overlays for Selectional Restrictions ---

(defn contextual-overlay
  "Constructs a contextual overlay that wraps a base spec registry with
   vertex-local selectional restrictions without mutating global coordinates.
   base-registry: map of {coord spec}
   overlay-rules: map of {coord specialized-spec}"
  [base-registry overlay-rules]
  (merge base-registry overlay-rules))

(defn evaluate-overlay
  "Evaluates referent against a contextual overlay registry."
  [overlay coord val]
  (if-let [pred (get overlay coord)]
    (boolean (pred val))
    false))

;; --- Inductive Habit Distillation (Mode-S -> Mode-D Bridge) ---

(defn infer-primitive-spec
  "Infers a basic predicate identifier from an observed empirical Clojure value."
  [val]
  (cond
    (integer? val) 'clojure.core/integer?
    (number? val)  'clojure.core/number?
    (string? val)  'clojure.core/string?
    (keyword? val) 'clojure.core/keyword?
    (boolean? val) 'clojure.core/boolean?
    (vector? val)  'clojure.core/vector?
    (set? val)     'clojure.core/set?
    (map? val)     'clojure.core/map?
    :else          'clojure.core/any?))

(defn infer-candidate-spec
  "Inductively distills a candidate schema definition from a Mode-S empirical corpus.
   Given a collection of referent maps, identifies consistently present keys and their
   most specific observed predicate habits."
  [corpus]
  (when (seq corpus)
    (let [all-keys (set (mapcat keys corpus))
          n (count corpus)
          ;; A key is required if present in 100% of corpus specimens
          req-keys (filterv (fn [k] (= n (count (filter #(contains? % k) corpus)))) all-keys)
          opt-keys (filterv (fn [k] (< (count (filter #(contains? % k) corpus)) n)) all-keys)
          ;; Infer candidate predicate per key
          key-preds (reduce
                      (fn [acc k]
                        (let [observed-vals (keep #(get % k) corpus)
                              preds (distinct (map infer-primitive-spec observed-vals))]
                          (assoc acc k (if (= 1 (count preds)) (first preds) 'clojure.core/any?))))
                      {}
                      all-keys)]
      {:inferred/req req-keys
       :inferred/opt opt-keys
       :inferred/preds key-preds})))

;; --- Unqualified Ingress Bridge & Mode-S Boundary Defense (CLJ-002-U, TELL-1002) ---

(defn normalize-raw-key
  "Converts a raw key (string or keyword) to a simple unqualified keyword name string."
  [k]
  (cond
    (keyword? k) (name k)
    (string? k) k
    :else (str k)))

(defn rehydrate-unqualified
  "Lifts an unqualified Mode-S external map (e.g. from JSON or Python dictionaries)
   into a sovereign namespaced Mode-D referent map (CLJ-002-U, TELL-1002).
   Accepts:
     raw-map: Map with string or unqualified keyword keys (e.g. {\"ipa\": \"/kæt/\"})
     target-ns-or-dict: Either a namespace prefix (symbol, string, or keyword)
                        or an explicit dictionary mapping {raw-key-or-str sovereign-coord-kw}.
   Options:
     :validate? - when true, validates conformed values against global spec registry (default true)
     :preserve-unmapped? - when true, retains keys not covered by mapping without namespace (default false)
   Returns:
     {:rehydrated/valid? boolean
      :rehydrated/referent sovereign-namespaced-map
      :rehydrated/anomalies map-of-coord-to-explain-data}."
  ([raw-map target-ns-or-dict]
   (rehydrate-unqualified raw-map target-ns-or-dict {}))
  ([raw-map target-ns-or-dict {:keys [validate? preserve-unmapped?] :or {validate? true preserve-unmapped? false}}]
   (let [ns-prefix (when (or (symbol? target-ns-or-dict) (keyword? target-ns-or-dict) (string? target-ns-or-dict))
                     (name target-ns-or-dict))
         explicit-map (when (map? target-ns-or-dict)
                        (reduce-kv (fn [m k v] (assoc m (normalize-raw-key k) v)) {} target-ns-or-dict))
         key-resolver (fn [raw-k]
                        (let [norm-k (normalize-raw-key raw-k)]
                          (if explicit-map
                            (get explicit-map norm-k)
                            (when ns-prefix
                              (keyword ns-prefix norm-k)))))]
     (loop [entries (seq raw-map)
            rehydrated {}
            anomalies {}]
       (if (empty? entries)
         (let [valid? (empty? anomalies)]
           {:rehydrated/valid? valid?
            :rehydrated/referent rehydrated
            :rehydrated/anomalies (when-not valid? anomalies)})
         (let [[k v] (first entries)
               sovereign-k (key-resolver k)]
           (if sovereign-k
             (let [spec (when validate? (s/get-spec sovereign-k))
                   spec-valid? (if spec (s/valid? spec v) true)
                   anomaly (when (and spec (not spec-valid?))
                             (s/explain-data spec v))]
               (recur (rest entries)
                      (assoc rehydrated sovereign-k v)
                      (if anomaly (assoc anomalies sovereign-k anomaly) anomalies)))
             (recur (rest entries)
                    (if preserve-unmapped? (assoc rehydrated k v) rehydrated)
                    anomalies))))))))

;; --- Differential Falsification Primitives (SEME-ARCH-007) ---

(defn boundary-specimen?
  "Checks if specimen is a boundary specimen that satisfies vertex-a but falsifies vertex-b."
  [vertex-a vertex-b specimen]
  (let [valid-a? (if (map? vertex-a)
                   (and (set/subset? (:vertex/req vertex-a) (set (keys specimen)))
                        (every? #(if-let [sp (s/get-spec %)] (s/valid? sp (get specimen %)) true)
                                (:vertex/req vertex-a)))
                   (s/valid? vertex-a specimen))
        valid-b? (if (map? vertex-b)
                   (and (set/subset? (:vertex/req vertex-b) (set (keys specimen)))
                        (every? #(if-let [sp (s/get-spec %)] (s/valid? sp (get specimen %)) true)
                                (:vertex/req vertex-b)))
                   (s/valid? vertex-b specimen))]
    (and valid-a? (not valid-b?))))

(defn shrink-map-specimen
  "Shrinks a failing map specimen by greedily removing keys while preserving
   the boundary invariant: (satisfies vertex-a) AND NOT (satisfies vertex-b)."
  [vertex-a vertex-b specimen]
  (let [removable-keys (keys specimen)]
    (reduce
      (fn [curr k]
        (let [smaller (dissoc curr k)]
          (if (boundary-specimen? vertex-a vertex-b smaller)
            smaller
            curr)))
      specimen
      removable-keys)))

(defn synthesize-boundary-specimen
  "Actively synthesizes a minimal boundary specimen that satisfies vertex-a
   while falsifying vertex-b (CLJ-009, CLJ-009-S, SEME-ARCH-007).
   Drives clojure.spec.gen / test.check against vertex-a with a boundary predicate filter,
   then shrinks the failing witness to isolate the exact structural chiasm.
   Options:
     :max-tries - maximum sampling attempts (default 200)
     :gen-override - optional custom generator for vertex-a."
  ([vertex-a vertex-b]
   (synthesize-boundary-specimen vertex-a vertex-b {}))
  ([vertex-a vertex-b {:keys [max-tries gen-override] :or {max-tries 200}}]
   (let [base-gen (or gen-override
                      (cond
                        (and (map? vertex-a) (contains? vertex-a :vertex/req))
                        (let [reqs (:vertex/req vertex-a)
                              opts (:vertex/opt vertex-a #{})]
                          (tc-gen/fmap
                            (fn [[req-vals opt-entries]]
                              (merge (zipmap reqs req-vals)
                                     (into {} opt-entries)))
                            (tc-gen/tuple
                              (apply tc-gen/tuple (map #(if (s/get-spec %) (s/gen %) tc-gen/string-alphanumeric) reqs))
                              (tc-gen/vector
                                (tc-gen/tuple (tc-gen/elements (vec (or (seq opts) [:opt/default])))
                                              tc-gen/string-alphanumeric)
                                0 3))))
                        (s/get-spec vertex-a) (s/gen vertex-a)
                        :else nil))]
     (if-not base-gen
       {:found? false :reason :no-generator-available}
       (let [samples (try (gen/sample base-gen max-tries) (catch Exception _ []))
             hit (some (fn [candidate]
                         (when (boundary-specimen? vertex-a vertex-b candidate)
                           candidate))
                       samples)]
         (if hit
           (let [shrunk (if (map? hit)
                          (shrink-map-specimen vertex-a vertex-b hit)
                          hit)]
             {:found? true
              :specimen shrunk
              :unshrunk hit
              :satisfies (if (map? vertex-a) (:vertex/id vertex-a :v/a) vertex-a)
              :falsifies (if (map? vertex-b) (:vertex/id vertex-b :v/b) vertex-b)})
           {:found? false :reason :no-boundary-discovered-within-budget}))))))
