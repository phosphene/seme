(ns phosphene.seme.figure
  "Roland Barthes' Nomadic Figures, Semic Constellations, and Symbolic Transgression.
   Formalizes:
     - The Figure (S/Z §XXVIII): proper names or conceptual loci acting as magnetic fields
       through which connotative semes migrate across discursive time.
     - trace-figure: tracks the temporal trajectory of semic coordinates around an actor or entity
       across sliding windows or lexia sequences.
     - detect-transgression (S/Z §XLI, §LXXXI): distinguishes lawful dialectical mediation
       (passing through negation: S_1 -> ~S_1 -> S_2) from symbolic transgression
       (illicit leap directly across the axis of contrariety into the Complex term S_1 + S_2)."
  (:require [clojure.set :as set]
            [clojure.spec.alpha :as s]
            [phosphene.seme.actant :as actant]))

;; --- 1. The Barthesian Figure ---

(defn make-figure
  "Constructs a Barthesian Figure record representing a nomadic semic locus.
   - id: Unique figure identifier (keyword, e.g. :figure/sarrasine, :figure/zambinella, :figure/faraday).
   - actor: Underlying actor or referent identifier (keyword, e.g. :actor/sarrasine).
   - semic-constellation: Set of connotative semes gathered around the figure.
   - trajectory: Ordered vector of time-indexed semantic coordinates or square positions.
   - metadata: Optional metadata map."
  ([id actor]
   (make-figure id actor #{} [] {}))
  ([id actor semic-constellation]
   (make-figure id actor semic-constellation [] {}))
  ([id actor semic-constellation trajectory]
   (make-figure id actor semic-constellation trajectory {}))
  ([id actor semic-constellation trajectory metadata]
   {:figure/id (if (keyword? id) id (keyword (str id)))
    :figure/actor actor
    :figure/semic-constellation (set semic-constellation)
    :figure/trajectory (vec trajectory)
    :figure/metadata metadata}))

(defn figure?
  "Returns true if x is a valid Barthesian figure record."
  [x]
  (and (map? x)
       (keyword? (:figure/id x))
       (some? (:figure/actor x))
       (set? (:figure/semic-constellation x))
       (vector? (:figure/trajectory x))))

(defn accumulate-semes
  "Accumulates new qualitative semes into the figure's semic constellation."
  [fig new-semes]
  (let [coll (cond
               (nil? new-semes) []
               (set? new-semes) new-semes
               (sequential? new-semes) (set new-semes)
               :else #{new-semes})]
    (update fig :figure/semic-constellation set/union coll)))

;; --- 2. Tracing Semic Trajectories over Discourse ---

(defn trace-figure
  "Traces the semic trajectory of an actor/figure across an ordered sequence of lexias
   or token windows.
   - fig: The base figure record (or id keyword).
   - lexias: Collection of lexia records (from phosphene.seme.barthes) or token maps.
   - square: Optional Greimassian semiotic square for geometric projection.
   Returns the updated figure with appended trajectory points and accumulated semes:
     {:step pos-int
      :lexia/id ...
      :semes #{...}
      :projection {:projection/position ... :projection/term ...}}"
  ([fig lexias]
   (trace-figure fig lexias nil))
  ([fig lexias square]
   (let [base-fig (if (figure? fig) fig (make-figure fig fig))
         actor-id (:figure/actor base-fig)
         actor-tok (if (keyword? actor-id) (keyword (name actor-id)) (keyword (str actor-id)))]
     (reduce
       (fn [current-fig [idx lexia]]
         (let [tokens (set (or (:lexia/tokens lexia) []))
               semic-codes (set (or (get-in lexia [:lexia/codes :code/semic]) []))
               ;; Check if actor is mentioned in tokens or metadata
               actor-present? (or (contains? tokens actor-tok)
                                  (contains? tokens actor-id)
                                  (= (get-in lexia [:lexia/metadata :actor]) actor-id)
                                  (contains? (set (or (get-in lexia [:lexia/codes :code/proairetic]) [])) actor-id))
               observed-semes (if actor-present?
                                (set/union semic-codes (set/intersection tokens #{:cold :warm :light :dark :fragile :force :field :current}))
                                semic-codes)]
           (if (and (not actor-present?) (empty? semic-codes))
             current-fig
             (let [proj (when (and square (ifn? square)) (square observed-semes))
                   point {:trajectory/step idx
                          :lexia/id (:lexia/id lexia)
                          :semes observed-semes
                          :projection proj}]
               (-> current-fig
                   (accumulate-semes observed-semes)
                   (update :figure/trajectory conj point))))))
       base-fig
       (map-indexed vector lexias)))))

;; --- 3. Symbolic Transgression Detection (Barthes S/Z §XLI, §LXXXI) ---

(defn detect-transgression
  "Distinguishes lawful dialectical mediation from symbolic transgression
   across a Greimassian semiotic square.
   - square: Semiotic square record (phosphene.paradigma.greimas).
   - step-from: Origin semantic term or square position.
   - step-to: Destination semantic term or square position.
   In Greimas's canonical narrative grammar, moving from contrary S_1 to S_2 requires
   lawful mediation through contradictory negation (~S_1):
     S_1 -(negation)-> ~S_1 -(assertion)-> S_2.
   In Barthes' symbolic code (S/Z), a Transgression occurs when an entity collapses
   contraries directly into the Complex term (S_1 + S_2) or leaps from S_1 directly
   to S_2 without traversing negation.
   Returns:
     {:transgression? boolean
      :type #{:lawful-mediation :transgressive-collapse :direct-contrary-leap :identity :unrelated}
      :from step-from
      :to step-to
      :square (:square/id square)}"
  [square step-from step-to]
  (let [s1 (:square/s1 square)
        s2 (:square/s2 square)
        non-s1 (:square/non-s1 square)
        non-s2 (:square/non-s2 square)
        complex-t (:square/complex square)
        neutral-t (:square/neutral square)]
    (cond
      (= step-from step-to)
      {:transgression? false
       :type :identity
       :from step-from
       :to step-to
       :square (:square/id square)}

      ;; Direct leap between contraries S_1 <-> S_2 without negation
      (or (and (= step-from s1) (= step-to s2))
          (and (= step-from s2) (= step-to s1)))
      {:transgression? true
       :type :direct-contrary-leap
       :from step-from
       :to step-to
       :square (:square/id square)}

      ;; Transgressive collapse: stepping from S_1 or S_2 directly into the Complex term
      (or (and (contains? #{s1 s2} step-from) (or (= step-to :complex) (= step-to complex-t)))
          (and (or (= step-from :complex) (= step-from complex-t)) (contains? #{s1 s2} step-to)))
      {:transgression? true
       :type :transgressive-collapse
       :from step-from
       :to step-to
       :square (:square/id square)}

      ;; Lawful dialectical steps
      (or
        ;; S_1 -> ~S_1 (negation)
        (and (= step-from s1) (= step-to non-s1))
        ;; S_2 -> ~S_2 (negation)
        (and (= step-from s2) (= step-to non-s2))
        ;; ~S_1 -> S_2 (assertion along deixis)
        (and (= step-from non-s1) (= step-to s2))
        ;; ~S_2 -> S_1 (assertion along deixis)
        (and (= step-from non-s2) (= step-to s1)))
      {:transgression? false
       :type :lawful-mediation
       :from step-from
       :to step-to
       :square (:square/id square)}

      :else
      {:transgression? false
       :type :unrelated
       :from step-from
       :to step-to
       :square (:square/id square)})))
