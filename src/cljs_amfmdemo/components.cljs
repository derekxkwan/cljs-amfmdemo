(ns cljs-amfmdemo.components
  (:require [reagent.core :as r]
            [reagent-forms.core :refer [bind-fields init-field value-of]]
            [cljs-amfmdemo.synths :as s]
            ))

(def cnv-prop {:w 500 :h 300})
(defonce app-state
  (r/atom {:am
           {:freq 0 :idx 0 :label "AM" :idx-label "index"
            :freq-lo 0 :freq-hi 1000 :freq-val 0
            :idx-lo 0 :idx-hi 100 :idx-val 0
            :freq-label "freq" :on false}
           :fm
           {:freq 0 :idx 0 :label "FM" :idx-label "index"
            :freq-lo 0 :freq-hi 1000 :freq-val 0
            :idx-lo 0 :idx-hi 100 :idx-val 0
            :freq-label "freq" :on false}
           :carrier
           {:freq 0 :idx 0 :label "carrier" :idx-label "vol"
            :freq-lo 30 :freq-hi 10000 :freq-val 300
            :idx-lo 0 :idx-hi 100 :idx-val 0
            :freq-label "freq" :on false}
           }))
                        

(defn provide-canvas []
  [:canvas {:id "cnv" :width (:w cnv-prop) :height (:h cnv-prop)}]
  )


(defn osc-idx-slider [osc]
  ;(.log js/console "idx")
  (let [entry (get @app-state osc)
        cur-lo (:idx-lo entry)
        cur-hi (:idx-hi entry)
        cur-val (:idx-val entry)
        label (:idx-label entry)
        ]
      {:type "range"
       :min cur-lo
       :max cur-hi
       :value cur-val
       :on-change #(s/idx-set! osc (-> % .-target .-value))
              }

    ))

(defn osc-freq-slider [osc]
  ;(.log js/console "freq")
  (let [entry (get @app-state osc)
        cur-lo (:freq-lo entry)
        cur-hi (:freq-hi entry)
        cur-val (:freq-val entry)
        label (:freq-label entry)]
    {:type "range"
     :min cur-lo
     :max cur-hi
     :value cur-val
     :on-change #(s/freq-set! osc (-> % .-target .-value))
              }
    ))
        


(defn osc-toggle [osc]
  ;(.log js/console "tgl")
  (let [entry (get @app-state osc)
        label (:label entry)
        cur-val (:on entry)]
    {:type "checkbox"
     :value cur-val
     :on-change #(s/toggler osc (-> % .-target .-value))
     }
    ))


(defn provide-sections []
 (doall (for [osc (keys @app-state)]

    [:div {:class "osc-section" :key (name osc)}
     [:label [:input (osc-toggle osc)] (get-in @app-state [osc :label])]
     [:br]
     [:label [:input (osc-freq-slider osc)] (get-in @app-state [osc :freq-label])]
     [:br]
     [:label [:input (osc-idx-slider osc)] (get-in @app-state [osc :idx-label])]
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
