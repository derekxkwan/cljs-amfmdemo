(ns cljs-amfmdemo.components
  (:require [reagent.core :as r]
            [cljs-amfmdemo.synths :as s]
            ))

(def cnv-prop {:w 500 :h 300})
(def audio-on? (r/atom false))
(def win js/window)

(defonce osc-state
  (r/atom {:rm
           {:toggle {:label "RM" :val false}
            :freq {:lo 0 :hi 1000 :val 0 :step 0.01 :label "freq"}
            :idx {:lo 0 :hi 100 :val 0 :step 0.01 :label "idx"}
            }
           :am
           {:toggle {:label "AM" :val false}
            :freq {:lo 0 :hi 1000 :val 0 :step 0.01 :label "freq"}
            :idx {:lo 0 :hi 100 :val 0 :step 0.01 :label "idx"}
            }
           :fm
           {:toggle {:label "FM" :on false}
            :freq {:lo 0 :hi 1000 :val 0 :step 0.01 :label "freq"}
            :idx {:lo 0 :hi 100 :val 0 :step 0.01 :label "idx"}
            }
           :carrier
           {:toggle {:label "carrier" :on false}
            :freq {:lo 20 :hi 5000 :val 0 :step 0.01 :label "freq"}
            :idx {:lo 0 :hi 100 :val 0 :step 0.01 :label "vol"}
            }
           }))
                        

(defn check-audio []
  (when (false? @audio-on?)
    (do (s/init-audio win (.getElementById js/document "cnv"))
        (reset! audio-on? true))))

(defn provide-header []
  [:h1 "interactive am/fm demo"]
  )

(defn provide-description []
  [:ul
   [:li "Amplitude Modulation (and its cousin Ring Modulation) and Frequency Modulation describe using signals to change (or modulate) of the named parameters of a given signal (known as the carrier). In this demo we are concerned with modulating the carrier's amplitude (in both AM and RM) and frequency (in FM"]
   [:li "AM/RM and FM introduce into the carrier signal additional frequency components called " [:strong " sidebands"] "."]
   [:li "The amplitude of the modulating signal (and thus how much it affects the carrier signal) is called the " [:strong  "index"] "."]
   [:li "To get started, click the checkbox for the carrier signal, which activates a sine wave"]
   ]
  )

(defn describe-rm []
  (when (true? (get-in @osc-state[:rm :toggle :val]))
    [:ul
     [:li "In ring modulation, the modulation signal is multiplied with the carrier signal"]
     [:li "At a fast enough rate (above the threshold of hearing at 20 Hz),this results in sidebands"]
     [:li "For every frequency component Fc in the carrier and every frequency component in the modulator Fm, we produce the sidebands at frequencies Fc-Fm and Fc + Fm."]
     [:li "Here the original frequency components Fc of the carrier are "
      [:strong  "missing"]
      "."]
     ]
    )
  )

(defn describe-am []
  (when (true? (get-in @osc-state[:am :toggle :val]))
    [:ul
     [:li "Amplitude modulation is very similar to ring modulation in where the modulation signal is multiplied with the carrier signal"]
     [:li "In this case, the modulation signal is "
      [:strong  "unipolar"]
      " (either all positive magnitude or all negative magnitude)"]
     [:li "AM produces the sidebands at frequencies Fc - Fm and Fc + Fm (as in with RM)"]
     [:li "However in this case, the original frequency components of the carrier are "
      [:strong  "present"] "."]
     ]
    )
  )

(defn describe-fm []
  (when (true? (get-in @osc-state[:fm :toggle :val]))

    [:ul
     [:li "In Frequency Modulation, the modulation signal modulates the carrier's frequency (often in the form carrier freq + (mod freq * mod index))"]
     [:li "For every frequency component Fc in the carrier signal  and every frequency component Fm in the modulator signal, FM produces sideband frequency components Fc + k*Fm where k is an integer (can be zero and negative"]
     [:li "In this case, the original frequency components of the carrier are "
      [:strong  "present"] "."]
     [:li "A very similar effect is obtained in Phase Modulation, where the modulation signal modulates the carrier's phase"]
     ]
    )
  )

(defn provide-canvas []
  [:canvas {:id "cnv" :width (:w cnv-prop) :height (:h cnv-prop)}]
  )


(defn osc-slider [osc param-type]
  ;(.log js/console "idx")
  (let [entry (get-in @osc-state [osc param-type])
        cur-lo (:lo entry)
        cur-hi (:hi entry)
        cur-val (:val entry)
        label (:label entry)
        step (:step entry)
        ]
      {:type "range"
       :min cur-lo
       :max cur-hi
       :default-value cur-val
       :on-change #(let [tval (-> % .-target .-value)]
                     (s/slider-set! osc param-type tval)
                     (reset! osc-state (assoc-in @osc-state [osc param-type :val] tval)))
       :key label
       :step step
              }

    ))
        
(defn osc-toggle [osc]
  ;(.log js/console "tgl")
  (let [entry (get-in @osc-state [osc :toggle])
        label (:label entry)
        cur-val (:val entry)]
    {:type "checkbox"
     :default-checked cur-val
     :on-change #(let [tval (-> % .-target .-checked)]
                     ;;(check-audio)
                     (s/toggler osc tval)
                     (reset! osc-state (assoc-in @osc-state [osc :toggle :val] tval)))
     :key label
     }
    ))


(defn provide-sections []
 (doall (for [osc (keys @osc-state)
          :let [labelfn
                #(let [cur-label (get-in @osc-state [osc % :label])]
                   (if (= % :toggle)
                     cur-label
                     (str cur-label ": " (get-in @osc-state [osc % :val]))))
                  ]]

    [:div {:class "osc-section" :key (name osc)}
     [:label [:input (osc-toggle osc)] (labelfn :toggle)]
     [:br]
     [:label [:input (osc-slider osc :freq)] (labelfn :freq)]
     [:br]
     [:label [:input (osc-slider osc :idx)] (labelfn :idx)]
     ]  
    ))
  )
     


(defn page []
 [:div
  [:div
   (provide-header)
   (provide-description)
   ]
   (provide-sections)
   (provide-canvas)
  [:div
   (describe-rm)
   [:br]
   (describe-am)
   [:br]
   (describe-fm)
   ]
  ]
)

(defn render []
  (r/render-component [page] (.getElementById js/document "app")))
