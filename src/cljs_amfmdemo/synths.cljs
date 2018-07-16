(ns cljs-amfmdemo.synths
  (:require ))

(defn toggler [osc value]
  (.log js/console value)
  )

(defn slider-set! [osc param-type value]
  (.log js/console value)
  )
