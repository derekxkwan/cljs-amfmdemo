
(ns cljs-amfmdemo.core
    (:require [cljs-amfmdemo.fft :as fft]
              [cljs-amfmdemo.components :as c]))

;(enable-console-print!)

(comment 
(def fft-size 4096)

(def win js/window)
(def ctx (or (win.AudioContext.) (win.webkitAudioContext.)))
(fft/set-canvas (.getElementById js/document "cnv"))
(fft/create-analyzer ctx fft-size)

(fft/set-mic-input ctx)
(fft/draw)
)

(c/render)
