(ns cljs-amfmdemo.synths
  (:require [cljs-amfmdemo.fft :as fft] ))

(def fft-size 1024)
(def ctx nil)
(def carrier {:osc nil :gain nil})
(def rmod (atom nil))
(def amod (atom nil))
(def fmod (atom nil))


(defn set-gain-at-time [cur-node cur-value cur-time]
  (.setValueAtTime (-> cur-node .-gain) (Math/min (/ cur-value 100) 1) cur-time)
  )

(defn set-freq-at-time [cur-node cur-freq cur-time]
   (.setValueAtTime (-> cur-node .-frequency) cur-freq cur-time)
  )
  

(defn carrier-create []
  (let [new-osc (.createOscillator. ctx)
        new-gain (.createGain. ctx)]
    (set! carrier (assoc carrier :osc new-osc))
    (set! carrier (assoc carrier :gain new-gain))
  )

  (let [cur-osc (get carrier :osc)
        cur-gain (get carrier :gain)]
    (.connect cur-osc cur-gain)
    (.connect cur-gain (.-destination  ctx))
    (set! (.-type cur-osc) "sine")
    )
  )

(defn carrier-handler [value freq idx]
  (let [cur-osc (get carrier :osc)
        cur-gain (get carrier :gain)
        cur-time (.-currentTime ctx)]
    (if (true? value)
      (do (set-gain-at-time cur-gain idx cur-time)
          (set-freq-at-time cur-osc freq cur-time)
          (.start cur-osc))
      (set-gain-at-time cur-gain 0 cur-time)
      )
    )
  )

(defn carrier-slider [param-type value]
    (let [cur-osc (get carrier :osc)
        cur-gain (get carrier :gain)
        cur-time (.-currentTime ctx)]
      (cond (= param-type :freq) (set-freq-at-time cur-osc value cur-time)
            :else (set-gain-at-time cur-gain value cur-time)
            )
      ))
  

(defn toggler [osc value freq idx]
  (cond (= osc :carrier) (carrier-handler value freq idx)
        :else nil)
  )

(defn slider-set! [osc param-type value]
  (cond (= osc :carrier) (carrier-slider param-type value)
        :else nil)
  )


(defn init-audio [win cnv]
  (set! ctx (if (.-AudioContext win) (win.AudioContext.) (win.webkitAudioContext.)))
  (carrier-create)
  (fft/init-fft ctx cnv fft-size (get carrier :gain))
  )
  
  
