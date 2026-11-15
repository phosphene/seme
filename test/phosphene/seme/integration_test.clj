(ns phosphene.seme.integration-test
  "Phase 5: Cross-Trifecta End-to-End Verification of Barthesian Semiotics.
   Validates the full analytic pipeline:
     Markdown AST (syntagma)
       -> Lexia Segmentation & Five-Code Braid (seme.barthes)
       -> Hermeneutic Retardation State Machine & Sanction Guard (seme.hermeneutic)
       -> Nomadic Figures & Transgression Detection (seme.figure)
       -> Actantial Narrative Programs (seme.actant)
       -> Lisible vs Scriptible Textuality & Tmesis (syntagma.textuality)
       -> Staggered Connotation & Order-2 Semiotic Square (paradigma.connotation)

   Empirical Public-Domain Fixtures:
     1. Michael Faraday (1832), 'Experimental Researches in Electricity' (Epistemic/Scientific Corpus)
     2. Honore de Balzac (1830), 'Sarrasine' (Barthes' Canonical Literary Corpus in S/Z)"
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            ;; Trifecta libraries
            [phosphene.syntagma.core :as syn]
            [phosphene.syntagma.io.markdown :as syn-md]
            [phosphene.syntagma.textuality :as syn-text]
            [phosphene.paradigma.core :as para]
            [phosphene.paradigma.greimas :as para-greimas]
            [phosphene.paradigma.connotation :as para-conn]
            [phosphene.seme.core :as seme]
            [phosphene.seme.barthes :as barthes]
            [phosphene.seme.hermeneutic :as herm]
            [phosphene.seme.figure :as figure]
            [phosphene.seme.actant :as actant]))

;; =============================================================================
;; 1. Public Domain Fixture: Michael Faraday (1832) Experimental Researches
;; =============================================================================

(def faraday-1832-md
  (str "# Experimental Researches in Electricity\n\n"
       "In the year 1831, Michael Faraday investigated the mystery of induced currents.\n\n"
       "Does magnetic force act at a distance through empty space?\n\n"
       "Faraday wrapped two insulated coils around a soft iron ring.\n\n"
       "When the voltaic battery circuit was connected, the galvanometer needle trembled.\n\n"
       "The magnetic lines curve continuously around the conducting wire.\n\n"
       "Therefore, electrical current is induced by moving lines of magnetic force."))

(deftest faraday-scientific-pipeline-test
  (testing "Faraday 1832: Full Cross-Trifecta Pipeline Verification"

    ;; --- Step 1: Syntagma Coordinate-Preserving Markdown Ingestion ---
    (let [blocks (syn-md/parse-markdown-blocks "faraday-1832.md" faraday-1832-md)]
      (is (= 7 (count blocks)) "1 heading + 6 distinct paragraphs")
      (is (= "faraday-1832.md:h:0" (:coordinate (first blocks))))
      (is (= "faraday-1832.md:p:1" (:coordinate (second blocks))))

      ;; --- Step 2: Seme Lexia Segmentation & Five-Code Braid ---
      (let [raw-lexias (barthes/segment-lexias blocks {:granularity :block})
            coder-fn (fn [l]
                       (case (:lexia/id l)
                         :faraday-1832.md:h:0#L0
                         {:code/cultural [:ref/royal-society]}

                         :faraday-1832.md:p:1#L0
                         {:code/hermeneutic [:hermeneutic/thematization]
                          :code/cultural    [:ref/year-1831]}

                         :faraday-1832.md:p:2#L0
                         {:code/hermeneutic [:hermeneutic/enigma]
                          :code/symbolic    [:antithesis/contact-vs-distance]}

                         :faraday-1832.md:p:3#L0
                         {:code/proairetic  [:act/construct-apparatus]}

                         :faraday-1832.md:p:4#L0
                         {:code/hermeneutic [:hermeneutic/partial-answer]
                          :code/proairetic  [:act/observe-needle-deflection]
                          :code/semic       [:seme/transient :seme/trembling]}

                         :faraday-1832.md:p:5#L0
                         {:code/semic       [:seme/curvature :seme/continuity]
                          :code/symbolic    [:symbol/field-lines]}

                         :faraday-1832.md:p:6#L0
                         {:code/hermeneutic [:hermeneutic/disclosure]
                          :code/proairetic  [:act/formulate-induction-law]
                          :code/cultural    [:ref/maxwell-precursor]}
                         {}))
            braided (barthes/braid-codes raw-lexias coder-fn)]

        (is (= 7 (count braided)))
        ;; Verify Mdrag coordinate preservation
        (is (every? #(str/starts-with? (name (:lexia/id %)) (:lexia/coordinate %)) braided))

        ;; Verify narrative unit classifications
        (is (= :index/informant (:lexia/unit-type (second braided))))   ;; p:1 with 'in the year'
        (is (= :function/nucleus (:lexia/unit-type (last braided))))    ;; p:6 with 'therefore'

        ;; Verify five-code polyphony
        (let [summary (barthes/braid-summary braided)]
          (is (= 7 (:summary/total-lexias summary)))
          (is (pos? (:summary/mean-polyphony summary)))
          (is (= 4 (get-in summary [:summary/code-frequencies :code/hermeneutic])))
          (is (= 3 (get-in summary [:summary/code-frequencies :code/proairetic])))
          (is (= 2 (get-in summary [:summary/code-frequencies :code/semic])))
          (is (= 2 (get-in summary [:summary/code-frequencies :code/symbolic])))
          (is (= 3 (get-in summary [:summary/code-frequencies :code/cultural]))))

        ;; --- Step 3: Hermeneutic Code Stream & Veridictory Retardation ---
        (let [herm-stream (barthes/extract-code-stream braided :code/hermeneutic)
              morphemes (mapv #(first (:values %)) herm-stream)]
          (is (= [:hermeneutic/thematization
                  :hermeneutic/enigma
                  :hermeneutic/partial-answer
                  :hermeneutic/disclosure]
                 morphemes))

          ;; Mid-investigation check (prior to disclosure): must be on Secret pole and not sanction-ready
          ;; (1 retardation morpheme: partial-answer, across 1 enigma -> ratio 1.0)
          (let [mid-state (herm/veridictory-state (subvec morphemes 0 3))]
            (is (= :open (:status mid-state)))
            (is (= :secret (:veridictory-pole mid-state)))
            (is (== 0.5 (:retardation-ratio mid-state)))
            (is (false? (:sanction-ready? mid-state))))

          ;; Conclusive investigation check: converges on Truth and sanction-ready
          (let [final-state (herm/veridictory-state morphemes)]
            (is (= :disclosed (:status final-state)))
            (is (= :truth (:veridictory-pole final-state)))
            (is (true? (:sanction-ready? final-state))))

          ;; Narrative sanction guarding
          (let [phase-map {:manipulation "Inquiry mandate"
                           :competence   "Apparatus"
                           :performance  "Experimental run"
                           :sanction     "Scientific truth assertion"}
                blocked (herm/guard-sanction phase-map (subvec morphemes 0 3))
                permitted (herm/guard-sanction phase-map morphemes)]
            (is (= :blocked (:status blocked)))
            (is (= :permitted (:status permitted)))))

        ;; --- Step 4: Nomadic Figure Tracking (Faraday as Scientific Figure) ---
        (let [fig (figure/make-figure :fig/faraday :actor/faraday)
              traced (figure/trace-figure fig braided nil)]
          (is (= :fig/faraday (:figure/id traced)))
          (is (contains? (:figure/semic-constellation traced) :seme/curvature))
          (is (contains? (:figure/semic-constellation traced) :seme/continuity)))

        ;; --- Step 5: Actantial Narrative Program Execution ---
        (let [initial-lack (actant/narrative-state :actor/faraday :disjunction :concept/induction-law)
              final-gain   (actant/narrative-state :actor/faraday :conjunction :concept/induction-law)
              pn (actant/narrative-program :pn/electromagnetic-induction :actor/faraday initial-lack final-gain)
              exec-res (actant/execute-program pn initial-lack)]
          (is (= :success (:status exec-res)))
          (is (= final-gain (:state/current exec-res)))

          ;; --- Step 6: Staggered Connotation Functor (Paradigma Order 1 -> Order 2) ---
          ;; Order 1 sign collapsed from the conjoined state
          (let [order-1-sign (para-conn/collapse-sign exec-res {:id :sign/induction-discovered :order 1})
                ;; Order 2 ideological/epistemic semiotic square
                field-ontology-sq (para-conn/lift-square
                                    :square/field-ontology
                                    :continuous-field :action-at-a-distance
                                    #{:sign/induction-discovered :concept/induction-law}
                                    #{:instantaneous-gravity :newtonian-corpuscle}
                                    {:order 2})
                projection (para-conn/connotative-projection field-ontology-sq [order-1-sign])]
            (is (= 2 (:projection/order projection)))
            (is (= :s1 (:projection/position projection)))
            (is (= :continuous-field (:projection/term projection)))
            (is (= [:sign/induction-discovered] (:projection/derived-from projection))))))

      ;; --- Step 7: Syntagma Textuality & Tmesis Diagnostics ---
      (let [paragraph-tokens (mapv :tokens (rest blocks))
            t-profile (syn-text/textuality-profile paragraph-tokens)]
        (is (<= 0.0 (:profile/lisible-index t-profile) 1.0))
        (is (<= 0.0 (:profile/scriptible-index t-profile) 1.0))
        (is (<= 0.0 (:profile/tmesis-mean t-profile) 1.0))
        (is (contains? #{:readerly :writerly :balanced} (:profile/classification t-profile)))))))

;; =============================================================================
;; 2. Public Domain Fixture: Honore de Balzac (1830) Sarrasine (S/Z Corpus)
;; =============================================================================

(def balzac-sarrasine-md
  (str "# Sarrasine\n\n"
       "I was deep in one of those daydreams which overtake the most thoughtless of men.\n\n"
       "At the Lanty mansion, an enigmatic old man appeared, resembling a walking phantom.\n\n"
       "Ernest de Lanty introduced the young sculptor Sarrasine to the opera of Rome.\n\n"
       "In Rome, Sarrasine fell madly in love with the prima donna Zambinella.\n\n"
       "Is Zambinella the ideal of feminine grace, or an illusion of the stage?\n\n"
       "Cardinal Cicognara had created a castrato, concealing the true being behind seeming.\n\n"
       "Sarrasine discovered the truth too late, leaping across the sacred barrier.\n\n"
       "Death claimed the artist when the illusion collapsed into horror."))

(deftest balzac-sarrasine-pipeline-test
  (testing "Balzac 1830 Sarrasine: Symbolic Transgression & Retardation Pipeline"

    (let [blocks (syn-md/parse-markdown-blocks "sarrasine-1830.md" balzac-sarrasine-md)
          raw-lexias (barthes/segment-lexias blocks {:granularity :block})
          coder-fn (fn [l]
                     (case (:lexia/id l)
                       :sarrasine-1830.md:h:0#L0
                       {:code/cultural [:ref/balzac-comedie-humaine]}

                       :sarrasine-1830.md:p:1#L0
                       {:code/hermeneutic [:hermeneutic/thematization]
                        :code/semic       [:seme/daydream :seme/reverie]}

                       :sarrasine-1830.md:p:2#L0
                       {:code/hermeneutic [:hermeneutic/enigma]
                        :code/symbolic    [:antithesis/life-vs-death]
                        :code/semic       [:seme/phantom]}

                       :sarrasine-1830.md:p:3#L0
                       {:code/proairetic  [:act/journey-to-rome]}

                       :sarrasine-1830.md:p:4#L0
                       {:code/proairetic  [:act/fall-in-love]
                        :code/semic       [:seme/passion]}

                       :sarrasine-1830.md:p:5#L0
                       {:code/hermeneutic [:hermeneutic/snare]
                        :code/symbolic    [:antithesis/masculine-vs-feminine]}

                       :sarrasine-1830.md:p:6#L0
                       {:code/hermeneutic [:hermeneutic/equivocation]
                        :code/cultural    [:ref/vatican-castrato]}

                       :sarrasine-1830.md:p:7#L0
                       {:code/hermeneutic [:hermeneutic/disclosure]
                        :code/symbolic    [:symbol/transgression]}

                       :sarrasine-1830.md:p:8#L0
                       {:code/proairetic  [:act/murder-sarrasine]
                        :code/symbolic    [:symbol/death]}
                       {}))
          braided (barthes/braid-codes raw-lexias coder-fn)]

      (is (= 9 (count braided)))

      ;; 1. Hermeneutic Code & Dilatory Retardation (S/Z §LXXXIX)
      (let [herm-stream (barthes/extract-code-stream braided :code/hermeneutic)
            morphemes (mapv #(first (:values %)) herm-stream)]
        (is (= [:hermeneutic/thematization
                :hermeneutic/enigma
                :hermeneutic/snare
                :hermeneutic/equivocation
                :hermeneutic/disclosure]
               morphemes))

        ;; Snare projects onto the Lie pole (:lie = Seeming + Non-Being)
        ;; (1 retardation morpheme: snare, out of 2 opening/enigma morphemes -> ratio 1/2 = 0.5)
        (let [snare-state (herm/veridictory-state (subvec morphemes 0 3))]
          (is (= :lie (:veridictory-pole snare-state)))
          (is (== 0.5 (:retardation-ratio snare-state))))

        ;; Full arc achieves disclosure (2 retardation morphemes: snare, equivocation, out of 2 opening/enigma -> ratio 2/2 = 1.0)
        (let [final-state (herm/veridictory-state morphemes)]
          (is (= :truth (:veridictory-pole final-state)))
          (is (== 1.0 (:retardation-ratio final-state)))
          (is (true? (:sanction-ready? final-state)))))

      ;; 2. S/Z Symbolic Transgression across Gender Semiotic Square (§XLI, §LXXXI)
      (let [gender-sq (para-greimas/semiotic-square :square/gender :masculine :feminine)
            ;; Lawful mediation: masculine -> non-masculine
            lawful (figure/detect-transgression gender-sq :masculine :non-masculine)
            ;; Direct contrary leap: masculine -> feminine without negation (Sarrasine's delusion)
            contrary-leap (figure/detect-transgression gender-sq :masculine :feminine)
            ;; Transgressive collapse: stepping directly into Complex term (Zambinella as castrato)
            castrato-collapse (figure/detect-transgression gender-sq :feminine :complex)]

        (is (false? (:transgression? lawful)))
        (is (= :lawful-mediation (:type lawful)))

        (is (true? (:transgression? contrary-leap)))
        (is (= :direct-contrary-leap (:type contrary-leap)))

        (is (true? (:transgression? castrato-collapse)))
        (is (= :transgressive-collapse (:type castrato-collapse))))

      ;; 3. Staggered Connotation (Order-2 Mythological Square)
      (let [order-1-sign (para-conn/collapse-sign
                           {:projection/position :complex
                            :projection/term :castrato
                            :projection/square :square/gender}
                           {:id :sign/castrato-illusion :order 1})
            myth-sq (para-conn/lift-square
                      :square/mythic-regime
                      :castration-and-death :natural-vitality
                      #{:sign/castrato-illusion :complex}
                      #{:natural-vitality :wholeness}
                      {:order 2})
            conn-proj (para-conn/connotative-projection myth-sq [order-1-sign])]
        (is (= 2 (:projection/order conn-proj)))
        (is (= :s1 (:projection/position conn-proj)))
        (is (= :castration-and-death (:projection/term conn-proj)))
        (is (= [:sign/castrato-illusion] (:projection/derived-from conn-proj)))))))
