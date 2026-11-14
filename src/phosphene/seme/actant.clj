(ns phosphene.seme.actant
  "Algirdas Julien Greimas's Actantial Syntax & Narrative Programs.
   Formalizes:
     - The Actantial Model (6 structural actantial roles across 3 orthogonal axes):
         * Axis of Desire/Volition: Subject <-> Object
         * Axis of Transmission/Knowledge: Sender (Destinateur) -> Object -> Receiver (Destinataire)
         * Axis of Power/Auxiliary Testing: Helper (Adjuvant) <-> Subject <-> Opponent (Opposant)
     - Actor-to-Actant Multi-Mapping (actors as referents embodying multiple actants)
     - Narrative State Junctions (Conjunction S ^ O vs Disjunction S v O)
     - Narrative Programs (Elementary transformations: F_trans(S_1 -> (S_2 ^ O)) or F_trans(S_1 -> (S_2 v O)))
     - Canonical Narrative Trajectory evaluation over token sequences or semantic states."
  (:require [clojure.set :as set]
            [clojure.spec.alpha :as s]))

;; --- 1. The Actantial Model ---

(def actant-roles
  "The 6 canonical structural actantial roles defined by Greimas."
  #{:subject :object :sender :receiver :helper :opponent})

(defn actantial-model
  "Constructs a Greimassian Actantial Model record.
   Binds structural actantial roles to actor identities or feature predicates.
   roles-map:
     {:subject  actor-or-pred
      :object   actor-or-pred
      :sender   actor-or-pred
      :receiver actor-or-pred
      :helper   actor-or-pred (or coll of)
      :opponent actor-or-pred (or coll of)}
   Optional metadata map."
  ([roles-map]
   (actantial-model roles-map {}))
  ([roles-map metadata]
   {:actants/roles (into {} (map (fn [[k v]]
                                   [k (if (and (contains? #{:helper :opponent} k)
                                               (not (coll? v)))
                                        [v]
                                        v)])
                                 roles-map))
    :actants/metadata metadata}))

(defn actantial-model?
  "Returns true if x is a valid Greimassian actantial model record."
  [x]
  (and (map? x)
       (map? (:actants/roles x))
       (contains? (:actants/roles x) :subject)
       (contains? (:actants/roles x) :object)))

(defn get-actant
  "Retrieves the actor(s) or predicate associated with a specific actantial role."
  [model role]
  (get-in model [:actants/roles role]))

(defn actors-for-role
  "Normalizes actant retrieval to a set of actors."
  [model role]
  (let [val (get-actant model role)]
    (cond
      (nil? val) #{}
      (coll? val) (set val)
      :else #{val})))

(defn find-roles-for-actor
  "Finds all structural actantial roles embodied by a given actor in the model.
   Expresses Greimas's fundamental insight: an actor is a syncretism of actants."
  [model actor]
  (into #{}
        (keep (fn [[role val]]
                (cond
                  (coll? val) (when (some #{actor} val) role)
                  (= val actor) role
                  :else nil)))
        (:actants/roles model)))

;; --- 2. Narrative States & Junctions ---

(defn narrative-state
  "Constructs a formal narrative state representing the junction between a Subject and an Object.
   - subject: actor or keyword identifier
   - junction: :conjunction (S ^ O, possession/union) or :disjunction (S v O, lack/separation)
   - object: entity, value, or objective"
  ([subject junction object]
   (narrative-state subject junction object {}))
  ([subject junction object metadata]
   {:state/subject subject
    :state/junction junction  ;; #{:conjunction :disjunction}
    :state/object object
    :state/metadata metadata}))

(defn narrative-state?
  "Returns true if x is a valid narrative state junction record."
  [x]
  (and (map? x)
       (contains? #{:conjunction :disjunction} (:state/junction x))
       (some? (:state/subject x))
       (some? (:state/object x))))

(defn conjoined?
  "Returns true if Subject is in conjunction with Object (S ^ O)."
  [state]
  (= :conjunction (:state/junction state)))

(defn disjoined?
  "Returns true if Subject is in disjunction with Object (S v O, expressing lack)."
  [state]
  (= :disjunction (:state/junction state)))

;; --- 3. Narrative Programs (Transformations of State) ---

(defn narrative-program
  "Constructs an elementary Narrative Program (PN):
   PN = F_transformation [ Operative-Subject -> (State-Target) ]
   - id: keyword identifier (e.g. :pn/quest, :pn/theft, :pn/sacrifice)
   - operative-subject: The actant who performs the transformation (S_op)
   - initial-state: Starting narrative state (e.g. S disjoined with O)
   - final-state: Target narrative state (e.g. S conjoined with O)
   - type: :acquisition (disjunction -> conjunction) or :deprivation (conjunction -> disjunction)."
  ([id operative-subject initial-state final-state]
   (let [trans-type (cond
                      (and (disjoined? initial-state) (conjoined? final-state)) :acquisition
                      (and (conjoined? initial-state) (disjoined? final-state)) :deprivation
                      :else :custom)]
     {:pn/id id
      :pn/operative-subject operative-subject
      :pn/initial initial-state
      :pn/final final-state
      :pn/type trans-type}))
  ([id operative-subject initial-state final-state metadata]
   (assoc (narrative-program id operative-subject initial-state final-state)
          :pn/metadata metadata)))

(defn narrative-program?
  "Returns true if x is a valid narrative program record."
  [x]
  (and (map? x)
       (keyword? (:pn/id x))
       (narrative-state? (:pn/initial x))
       (narrative-state? (:pn/final x))
       (contains? #{:acquisition :deprivation :custom} (:pn/type x))))

(defn execute-program
  "Executes a narrative program upon an existing state.
   Validates that the current state matches the initial state conditions.
   Returns {:status :success :new-state final-state} or {:status :failed :reason ...}."
  [pn current-state]
  (if (and (= (:state/subject current-state) (get-in pn [:pn/initial :state/subject]))
           (= (:state/object current-state) (get-in pn [:pn/initial :state/object]))
           (= (:state/junction current-state) (get-in pn [:pn/initial :state/junction])))
    {:status :success
     :pn/id (:pn/id pn)
     :state/previous current-state
     :state/current (:pn/final pn)}
    {:status :failed
     :pn/id (:pn/id pn)
     :reason :initial-state-mismatch
     :expected (:pn/initial pn)
     :actual current-state}))

;; --- 4. Canonical Narrative Trajectory (The 4 Phases) ---

(def canonical-phases
  "Greimas's 4 stages of the Canonical Narrative Schema:
   1. :manipulation (Contract / Mandate: Sender induces Subject to act)
   2. :competence   (Acquisition of modal capacity: Having to, Wanting to, Knowing how, Being able to do)
   3. :performance  (The Main Act: Transformation of state junction)
   4. :sanction     (Evaluation / Recognition: Sender judges performance)"
  [:manipulation :competence :performance :sanction])

(defn canonical-trajectory
  "Evaluates an ordered sequence of events or states against the 4 phases of
   Greimas's Canonical Narrative Schema.
   Returns a conformed trajectory map with matched phases and residual."
  [phase-map]
  (let [matched (into {} (filter (fn [[k v]] (and (contains? (set canonical-phases) k) (some? v))) phase-map))
        missing (set/difference (set canonical-phases) (set (keys matched)))]
    {:trajectory/phases matched
     :trajectory/complete? (empty? missing)
     :trajectory/missing missing
     :trajectory/order canonical-phases}))
