(ns cljs-amfmdemo.components
  (:require [reagent.core :as r]
            [reagent-forms.core :refer [bind-fields init-field value-of]]
            [cljs-amfmdemo.synths :as s]
            ))

(def cnv-prop {:w 500 :h 300})
(defonce app-state
  (r/atom {:am
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
                        



(defn provide-canvas []
  [:canvas {:id "cnv" :width (:w cnv-prop) :height (:h cnv-prop)}]
  )


(defn osc-slider [osc param-type]
  ;(.log js/console "idx")
  (let [entry (get-in @app-state [osc param-type])
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
                     (reset! app-state (assoc-in @app-state [osc param-type :val] tval)))
       :key label
       :step step
              }

    ))
        
(defn osc-toggle [osc]
  ;(.log js/console "tgl")
  (let [entry (get-in @app-state [osc :toggle])
        label (:label entry)
        cur-val (:val entry)]
    {:type "checkbox"
     :default-checked cur-val
     :on-change #(let [tval (-> % .-target .-checked)]
                     (s/toggler osc tval)
                     (reset! app-state (assoc-in @app-state [osc :toggle :val] tval)))
     :key label
     }
    ))


(defn provide-sections []
 (doall (for [osc (keys @app-state)
          :let [labelfn
                #(let [cur-label (get-in @app-state [osc % :label])]
                   (if (= % :toggle)
                     cur-label
                     (str cur-label ": " (get-in @app-state [osc % :val]))))
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
   (provide-sections)
   (provide-canvas)
   ]
  )

(defn render []
(r/render-component [page] (.getElementById js/document "app")))
