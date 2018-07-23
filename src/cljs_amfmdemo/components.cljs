(ns cljs-amfmdemo.components
  (:require [reagent.core :as r]
            [cljs-amfmdemo.synths :as s]
            ))

(def audio-on? (r/atom false))
(def win js/window)

(def osc-types ["sine" "square" "sawtooth" "triangle"])

(defonce osc-state
  (r/atom { :carrier
           {:toggle {:label "carrier signal" :val false}
            :freq {:lo 30 :hi 10000 :val 0 :step 0.01 :label "freq"}
            :idx {:lo 0 :hi 100 :val 0 :step 0.01 :label "vol"}
            :type "sine"
            }
           :rmod
           {:toggle {:label "ring modulation" :val false}
            :freq {:lo 0 :hi 5000 :val 0 :step 0.01 :label "freq"}
            :idx {:lo 0 :hi 100 :val 0 :step 0.01 :label "index"}
            :type "sine"
            }
           :amod
           {:toggle {:label "amplitude modulation" :val false}
            :freq {:lo 0 :hi 5000 :val 0 :step 0.01 :label "freq"}
            :idx {:lo 0 :hi 100 :val 0 :step 0.01 :label "index"}
            :type "sine"
            }
           :fmod
           {:toggle {:label "frequency modulation" :on false}
            :freq {:lo 0 :hi 5000 :val 0 :step 0.01 :label "freq"}
            :idx {:lo 0 :hi 100000 :val 0 :step 0.01 :label "index"}
            :type "sine"
            }
           }))
                        

(defn check-audio []
  (when (false? @audio-on?)
    (do (s/init-audio win (.getElementById js/document "cnv"))
        (reset! audio-on? true))))

(defn provide-header []
  [:div {:align "center" :id "header"}
   [:h1 "interactive am/fm demo (by derek kwan)"]
   [:a {:href "https://gitlab.com/derekxkwan/cljs-amfmdemo"} "(source, gpl v3)"]
   ]
  )

(defn provide-description []
  [:ul
   [:li "Amplitude modulation (and its cousin ring modulation) and Frequency Modulation describe using signals to change (or modulate) of the named parameters of a given signal (known as the carrier). In this demo we are concerned with modulating the carrier's amplitude (in both AM and RM) and frequency (in FM"]
   [:li "AM/RM and FM introduce into the carrier signal additional frequency components called " [:strong " sidebands"] "."]
   [:li "The amplitude of the modulating signal (and thus how much it affects the carrier signal) is called the " [:strong  "index"] " (here scaled by 100)."]
   [:li [:strong "To get started"] ", click the checkbox for the carrier signal, which activates a sine wave oscillator."]
   ]
  )

(defn describe-carrier []
  (when (true? (get-in @osc-state[:carrier :toggle :val]))
    [:ul {:class "carrier notediv"}
     [:li "Every signal that is " [:strong "periodic"] " (is repeating and in terms of sound, has pitch) can be deconstructed into distinct frequency components using the Fourier theorem"]
     [:li "From this frequency component, " [:strong "sinusoidal (sine, cosine)"] " waves are considered to be the most elemental signals, consisting of one frequency component at its fundamental frequency (a periodic signal's lowest rate of repetition"]
     [:li "Thus, periodic signals can be seen as built up of sine waves"]
     [:li "Less smoothly oscillating signals (such as square waves and sawtooth waves) have several frequency components"]
     [:li "More regular a signal's repetitions results in the additional frequency components adhering closer to integer multiple relationships with a signal's fundamental frequency (components oscillating at two times a signal's fundamental, three times, four times, five times...)"]
     [:li "Note that when you introduce sidebands via one of the modulation techniques, the components will \"reflect back\" beyond the lower (0 Hz) and upper (half the sampling rate) bounds of the Frequency Analysis window. This is called " [:strong "aliasing"] "."]
      ]
    )
  )

(defn describe-rm []
  (when (true? (get-in @osc-state[:rmod :toggle :val]))
    [:ul {:class "rmod notediv"}
     [:li "In " [:strong "ring modulation"] ", the modulation signal is multiplied with the carrier signal"]
     [:li "At a fast enough rate (above the threshold of hearing at 20 Hz),this results in sidebands"]
     [:li "For every frequency component Fc in the carrier and every frequency component in the modulator Fm, we produce the sidebands at frequencies Fc-Fm and Fc + Fm."]
     [:li "Here the original frequency components Fc of the carrier are "
      [:strong  "missing"]
      "."]
     [:li "Note that you have to set a non-zero index to produce sound (since RM involves multiplication)."]
     ]
    )
  )

(defn describe-am []
  (when (true? (get-in @osc-state[:amod :toggle :val]))
    [:ul {:class "amod notediv"}
     [:li [:strong "Amplitude modulation"] " is very similar to ring modulation in where the modulation signal is multiplied with the carrier signal"]
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
  (when (true? (get-in @osc-state[:fmod :toggle :val]))

    [:ul {:class "fmod notediv"}
     [:li "In " [:strong "frequency modulation"] ", the modulation signal modulates the carrier's frequency (often in the form carrier freq + (mod freq * mod index))"]
     [:li "For every frequency component Fc in the carrier signal  and every frequency component Fm in the modulator signal, FM produces sideband frequency components Fc + k*Fm where k is an integer (can be zero and negative"]
     [:li "In this case, the original frequency components of the carrier are "
      [:strong  "present"] "."]
     [:li "A very similar effect is obtained in Phase Modulation, where the modulation signal modulates the carrier's phase"]
     ]
    )
  )

(defn provide-canvas []
  [:canvas {:id "cnv" :style {:width "90%" :height "90%"}}]
  )


(defn osc-select [osc]
  [:form {:id (str (name osc) "-radio")
          :on-change #(let [tval (-> % .-target .-value)]
                        (swap! osc-state assoc-in [osc :type] tval)
                        (s/osc-type-set osc tval))
          }
   (doall (for [i osc-types
                :let [cur-id (str (name osc) "-" i)]
                ]
            [:label {:key cur-id}
             [:input {:type "radio" :name (str (name osc) "-radio") :value i :key cur-id :default-checked (= i "sine")}]
             i ]
            ))
   ]
  )
           


(defn osc-slider [osc param-type]
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
       :on-change #(let [tval (-> % .-target .-value)
                         on? (get-in @osc-state [osc :toggle :val])]
                     (when (true? @audio-on?) (s/slider-set! osc param-type tval on?))
                     (swap! osc-state assoc-in [osc param-type :val] tval)
                     )
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
     :on-change #(let [tval (-> % .-target .-checked)
                       tfreq (get-in @osc-state [osc :freq :val])
                       tidx (get-in @osc-state [osc :idx :val])
                       ttype (get-in @osc-state [osc :type])
                       ]
                     (check-audio)
                     (s/toggler osc tval tfreq tidx ttype)
                     (swap! osc-state assoc-in [osc :toggle :val] tval))
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
    [:div {:key (name osc) :class (name osc)}
     [:label [:input (osc-toggle osc)] (labelfn :toggle)]
     [:br]
     (osc-select osc)
     [:br]
     [:label [:input (osc-slider osc :freq)] (labelfn :freq)]
     [:br]
     [:label [:input (osc-slider osc :idx)] (labelfn :idx)]
     ]  
    ))
  )
     


(defn page []
 [:div
   (provide-header)
  [:div {:align "left" :id "descrip"}
   (provide-description)
   ]
  [:div {:style {:width "90%"  :display "flex"}}
   [:div {:style {:width "50%" :id "ctrl-panel"}}
    (provide-sections)
    ]
   [:div { :id "canvas-panel" :align "center"}
    "Frequency Analysis"
    (provide-canvas)
    ]
   ]
  [:div {:id "notes" :width "90%"}
   (describe-carrier)
   (describe-rm)
   (describe-am)
   (describe-fm)
   ]
  ]
)

(defn render []
  (r/render-component [page] (.getElementById js/document "app")))
