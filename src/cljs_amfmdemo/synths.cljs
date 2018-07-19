(ns cljs-amfmdemo.synths
  (:require [cljs-amfmdemo.fft :as fft] ))

(def fft-size 1024)
(def ctx nil)


(defn toggler [osc value]
  (.log js/console value)
  )

(defn slider-set! [osc param-type value]
  (.log js/console value)
  )


(defn init-audio [win cnv]
  (set! ctx (if (.-AudioContext win) (win.AudioContext.) (win.webkitAudioContext.)))
  (fft/init-fft ctx cnv fft-size)
  )
  
  
