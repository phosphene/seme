(ns phosphene.seme.rheme
  "Peirce's Firstness & Structuralist Componential Semantics.
   Models semes as unsaturated qualitative predicates and minimal distinctive
   semantic features, bundling them into sememes without closed-world classes."
  (:require [clojure.set :as set]
            [clojure.spec.alpha :as s]))

;; --- Seme: Unsaturated Feature Predicate ---

(defn seme
  "Constructs an unsaturated semantic feature record (seme).
   id: Sovereign namespaced keyword (e.g. :seme.feat/telic, :seme.ont/animate).
   pred: Pure predicate function (fn [val] boolean?).
   metadata: Optional descriptive metadata map."
  ([id pred]
   (seme id pred {}))
  ([id pred metadata]
   {:seme/id id
    :seme/pred pred
    :seme/metadata metadata}))

(defn seme?
  "Returns true if x satisfies the seme record invariant."
  [x]
  (and (map? x)
       (keyword? (:seme/id x))
       (ifn? (:seme/pred x))))

(defn test-seme
  "Tests whether a specific value satisfies the unsaturated seme predicate."
  [seme val]
  (boolean ((:seme/pred seme) val)))

;; --- Sememe: Distinctive Feature Bundle ---

(defn sememe
  "Constructs a sememe as a bundle of distinctive semes indexed by their sovereign id.
   Represents the componential content structure of an analytical vertex or sign."
  ([semes-coll]
   (sememe semes-coll {}))
  ([semes-coll metadata]
   {:sememe/semes (reduce (fn [acc s] (assoc acc (:seme/id s) s)) {} semes-coll)
    :sememe/metadata metadata}))

(defn sememe?
  "Returns true if x is a valid sememe bundle."
  [x]
  (and (map? x)
       (map? (:sememe/semes x))
       (every? seme? (vals (:sememe/semes x)))))

(defn bundle-semes
  "Combines multiple sememes or semes into a unified sememe bundle."
  [& items]
  (let [all-semes (mapcat (fn [item]
                            (cond
                              (sememe? item) (vals (:sememe/semes item))
                              (seme? item) [item]
                              (sequential? item) item
                              :else []))
                          items)]
    (sememe all-semes)))

(defn feature-ids
  "Returns the set of sovereign feature keyword IDs present in a sememe bundle."
  [sememe]
  (set (keys (:sememe/semes sememe))))

(defn plerematic-difference
  "Computes the distinctive feature differential (symmetric difference) between two sememes.
   Returns {:only-a set-of-ids :only-b set-of-ids :shared set-of-ids}."
  [sememe-a sememe-b]
  (let [ids-a (feature-ids sememe-a)
        ids-b (feature-ids sememe-b)]
    {:only-a (set/difference ids-a ids-b)
     :only-b (set/difference ids-b ids-a)
     :shared (set/intersection ids-a ids-b)}))
