(ns phosphene.seme.hermeneutic
  "Roland Barthes' Hermeneutic Code & Greimas's Veridictory Retardation State Machine.
   Formalizes:
     - The Hermeneutic Morphemes (S/Z §XXXII, §XXXVII, §LXXXIX):
         * :thematization / :proposal - initial suggestion of an unresolved question
         * :enigma - formal posing of the enigma (opening disjunction S v O_truth)
         * :promise - promise of a future answer
         * :snare (leurre) - deceptive answer projecting onto the Lie pole (Seeming + Non-Being)
         * :equivocation (equivoque) - double-edged answer suspending resolution in Being + Seeming
         * :jamming (blocage) - declaration of insolubility, collapsing into Falsity (Non-Being + Non-Seeming)
         * :suspended-answer - holding inquiry in abeyance
         * :partial-answer - vertical deixis step onto the Secret pole (Being + Non-Seeming)
         * :disclosure (devoilement) - irreversible convergence onto Truth (Being + Seeming)
     - Mapping onto Greimas's Veridictory Square (:truth :falsity :secret :lie)
     - Retardation ratio (dilatoire): structural delay per posed enigma
     - guard-sanction: protects canonical narrative trajectories against premature sanction."
  (:require [clojure.set :as set]
            [clojure.spec.alpha :as s]
            [phosphene.seme.actant :as actant]))

;; --- The Canonical Hermeneutic Morphemes ---

(def hermeneutic-morphemes
  "The 9 canonical morphemes of the hermeneutic code defined in Barthes' S/Z (§LXXXIX)."
  #{:hermeneutic/thematization
    :hermeneutic/proposal
    :hermeneutic/enigma
    :hermeneutic/promise
    :hermeneutic/snare
    :hermeneutic/equivocation
    :hermeneutic/jamming
    :hermeneutic/suspended-answer
    :hermeneutic/partial-answer
    :hermeneutic/disclosure})

(def retardation-morphemes
  "Hermeneutic morphemes that actively retard or divert epistemic disclosure."
  #{:hermeneutic/snare
    :hermeneutic/equivocation
    :hermeneutic/jamming
    :hermeneutic/suspended-answer
    :hermeneutic/partial-answer})

(def veridictory-poles
  "The 4 positions on Greimas's Veridictory Square."
  #{:truth :falsity :secret :lie :unformulated})

;; --- State Machine & Veridictory Mapping ---

(defn morpheme->veridictory-pole
  "Maps a single hermeneutic morpheme onto its corresponding veridictory position
   on Greimas's Veridictory Square:
     - :snare (leurre)           -> :lie      (Seeming + Non-Being: S_2 + ~S_1)
     - :partial-answer           -> :secret   (Being + Non-Seeming: S_1 + ~S_2)
     - :equivocation (equivoque) -> :truth    (Double-edged Being + Seeming, but suspended)
     - :jamming (blocage)        -> :falsity  (Non-Being + Non-Seeming: ~S_1 + ~S_2)
     - :disclosure (devoilement) -> :truth    (Conclusive Being + Seeming: S_1 + S_2)
     - otherwise                 -> :secret   (Inquiry open, ground not fully apparent)"
  [morpheme]
  (case morpheme
    :hermeneutic/snare :lie
    :hermeneutic/partial-answer :secret
    :hermeneutic/equivocation :truth
    :hermeneutic/jamming :falsity
    :hermeneutic/disclosure :truth
    :secret))

(defn veridictory-state
  "Evaluates an ordered sequence of hermeneutic morphemes (or lexias containing them)
   and computes the current veridictory state across Greimas's Veridictory Square.
   Returns:
     {:status #{:unformulated :open :jammed :disclosed}
      :veridictory-pole #{:truth :falsity :secret :lie :unformulated}
      :enigmas-posed pos-int
      :retardation-count int
      :retardation-ratio double
      :last-morpheme keyword?
      :sanction-ready? boolean?}"
  [morphemes]
  (let [cleaned-morphemes (mapv (fn [m]
                                  (cond
                                    (keyword? m) m
                                    (and (map? m) (contains? m :code/hermeneutic))
                                    (first (get m :code/hermeneutic))
                                    (and (map? m) (contains? m :morpheme))
                                    (:morpheme m)
                                    :else (keyword "hermeneutic" (name m))))
                                (or morphemes []))
        total-m (count cleaned-morphemes)
        last-m (last cleaned-morphemes)
        enigma-count (count (filter #{:hermeneutic/enigma :hermeneutic/proposal :hermeneutic/thematization} cleaned-morphemes))
        retardation-count (count (filter retardation-morphemes cleaned-morphemes))
        has-enigma? (pos? enigma-count)
        is-disclosed? (some #{:hermeneutic/disclosure} cleaned-morphemes)
        is-jammed? (= last-m :hermeneutic/jamming)
        current-pole (cond
                       (empty? cleaned-morphemes) :unformulated
                       is-disclosed? :truth
                       is-jammed? :falsity
                       :else (morpheme->veridictory-pole last-m))
        status (cond
                 (not has-enigma?) :unformulated
                 is-disclosed? :disclosed
                 is-jammed? :jammed
                 :else :open)
        retard-ratio (if (pos? enigma-count)
                       (double (/ retardation-count enigma-count))
                       (double retardation-count))]
    {:status status
     :veridictory-pole current-pole
     :enigmas-posed enigma-count
     :retardation-count retardation-count
     :retardation-ratio retard-ratio
     :last-morpheme last-m
     :sanction-ready? (= status :disclosed)}))

(defn retardation-ratio
  "Calculates the structural delay ratio (dilatoire): the number of retardation
   morphemes per posed enigma across a sequence."
  [morphemes]
  (:retardation-ratio (veridictory-state morphemes)))

(defn hermeneutic-arc
  "Constructs a formal hermeneutic arc record tracking the full progression of an inquiry.
   id: Sovereign keyword identifier (e.g. :arc/mystery-of-sarrasine, :arc/induction-mechanism).
   enigma-label: Descriptive string or keyword.
   morphemes: Vector of hermeneutic morphemes."
  ([id enigma-label]
   (hermeneutic-arc id enigma-label []))
  ([id enigma-label morphemes]
   (let [v-state (veridictory-state morphemes)]
     {:arc/id (if (keyword? id) id (keyword (str id)))
      :arc/enigma enigma-label
      :arc/morphemes (vec morphemes)
      :arc/state v-state})))

(defn step-arc
  "Appends a new hermeneutic morpheme to an active hermeneutic arc,
   updating the veridictory state deterministically."
  [arc morpheme]
  (let [new-morphemes (conj (:arc/morphemes arc) morpheme)]
    (assoc arc
           :arc/morphemes new-morphemes
           :arc/state (veridictory-state new-morphemes))))

;; --- Guarding Narrative Sanction against Premature Closure ---

(defn guard-sanction
  "Composes an epistemic trajectory or phase-map with Greimas's Canonical Narrative Schema.
   Guarantees that an epistemic narrative program cannot enter the :sanction phase
   until the underlying hermeneutic inquiry has reached :hermeneutic/disclosure.
   If premature sanction is attempted while in a retardation state (:snare, :equivocation, :jamming, or :open),
   returns a gated report indicating failure and the active veridictory violation."
  [phase-map hermeneutic-sequence]
  (let [v-state (veridictory-state hermeneutic-sequence)
        has-sanction? (contains? phase-map :sanction)]
    (if (and has-sanction? (not (:sanction-ready? v-state)))
      {:status :blocked
       :reason :premature-sanction-attempted
       :veridictory-state v-state
       :phase-map (dissoc phase-map :sanction)
       :canonical-trajectory (actant/canonical-trajectory (dissoc phase-map :sanction))}
      {:status :permitted
       :reason :sanction-epistemically-grounded
       :veridictory-state v-state
       :phase-map phase-map
       :canonical-trajectory (actant/canonical-trajectory phase-map)})))
