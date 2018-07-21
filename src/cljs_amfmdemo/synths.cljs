(ns cljs-amfmdemo.synths
  (:require [cljs-amfmdemo.fft :as fft] ))

(def fft-size 1024)
(def ctx nil)

(def oscs {:carrier {:osc nil :gain-rmod nil :gain-amod nil :gain nil}
           :rmod  {:osc nil :gain nil}
           :amod  {:osc nil :gain nil}
           :fmod  {:osc nil :gain nil}})


(defn set-gain-at-time [cur-node cur-value cur-time]
  (.setValueAtTime (-> cur-node .-gain) (/ cur-value 100) cur-time)
  )

(defn set-freq-at-time [cur-node cur-freq cur-time]
   (.setValueAtTime (-> cur-node .-frequency) cur-freq cur-time)
  )


(defn get-current-time []
  (.-currentTime ctx))

(defn osc-create [osc]
 (let [new-osc (.createOscillator. ctx)
       new-gain (.createGain. ctx)]
    (set! oscs (assoc-in oscs [osc :osc] new-osc))
    (set! oscs (assoc-in oscs [osc :gain] new-gain))
    (.start (get-in oscs [osc :osc]))
    (if (= 'carrier (symbol (name osc)))
      (let [new-rg (.createGain. ctx)
            new-ag (.createGain. ctx)]
        (set! oscs (assoc-in oscs [osc :gain-rmod] new-rg))
        (set! oscs (assoc-in oscs [osc :gain-amod] new-ag))
        )
      (let [cur-osc (get-in oscs [osc :osc])
            cur-gain (get-in oscs [osc :gain])]
        (.connect cur-osc cur-gain)
        )
      )
    ))
   


(defn carrier-connect []
  (let [carrier (get oscs :carrier)
        cur-osc (get carrier :osc)
        cur-gain (get carrier :gain)
        cur-rg (get carrier :gain-rmod)
        cur-ag (get carrier :gain-amod)]
    (.connect cur-osc cur-rg)
    (.connect cur-rg cur-ag)
    (.connect cur-ag cur-gain)
    (.connect cur-gain (.-destination  ctx))
    (set! (.-type cur-osc) "sine")
    (set-gain-at-time cur-rg 100 (get-current-time))
    (set-gain-at-time cur-ag 100 (get-current-time))
    )
  )

(defn rmod-connect [connect? freq idx]
  (let [rmod-gain (get-in oscs [:rmod :gain])
        rmod-osc (get-in oscs [:rmod :osc])
        carrier-rmod (get-in oscs [:carrier :gain-rmod])
        cur-time (get-current-time)]
    (if (true? connect?)
      (do (.connect rmod-gain (.-gain carrier-rmod))
          (set-freq-at-time rmod-osc freq cur-time)
          (set-gain-at-time rmod-gain idx cur-time)
          (set-gain-at-time carrier-rmod 0 cur-time) ;; hooking this up SUMS with the currently set value so we want to set to 0 
          )
      (do (.disconnect rmod-gain (.-gain carrier-rmod))
          (set-gain-at-time carrier-rmod 100 cur-time))
      )
    ))

(defn amod-connect [connect? freq idx]
  (let [amod-gain (get-in oscs [:amod :gain])
        amod-osc (get-in oscs [:amod :osc])
        carrier-amod (get-in oscs [:carrier :gain-amod])
        cur-time (get-current-time)]
    (set-gain-at-time carrier-amod 100 cur-time)
    (if (true? connect?)
      (do (.connect amod-gain (.-gain carrier-amod))
          (set-freq-at-time amod-osc freq cur-time)
          (set-gain-at-time amod-gain idx cur-time))
      (.disconnect amod-gain (.-gain carrier-amod))
      )
    ))

(defn fmod-connect [connect? freq idx]
  (let [fmod-gain (get-in oscs [:fmod :gain])
        fmod-osc (get-in oscs [:fmod :osc])
        carrier-osc(get-in oscs [:carrier :osc])
        cur-time (get-current-time)]
    (if (true? connect?)
      (do (.connect fmod-gain (.-frequency carrier-osc))
          (set-freq-at-time fmod-osc freq cur-time)
          (set-gain-at-time fmod-gain idx cur-time))
      (.disconnect fmod-gain (.-frequency carrier-osc))
      )
    ))

;; resolve doesn't exist in cljs =(
(defn osc-connect [osc connect? freq idx]
  (let [osc-string (name osc)]
    (cond (= "carrier" osc-string) (carrier-connect)
          (= "fmod" osc-string) (fmod-connect connect? freq idx)
          (= "amod" osc-string) (amod-connect connect? freq idx)
          :else (rmod-connect connect? freq idx)
        )
    ))

(defn toggler [want-osc value freq idx]
  (let [osc (get oscs want-osc) 
        cur-osc (get osc :osc)
        cur-gain (get osc :gain)
        cur-time (get-current-time)]
    (if (true? value)
      (do (set-gain-at-time cur-gain idx cur-time)
          (set-freq-at-time cur-osc freq cur-time)
          )
      (set-gain-at-time cur-gain 0 cur-time)
      )
    (when-not (= 'carrier (symbol (name want-osc)))
      (osc-connect want-osc value freq idx))
    )
  )

(defn slider-set! [want-osc param-type value on?]
    (let [osc (get oscs want-osc)
          cur-osc (get osc :osc)
          cur-gain (get osc :gain)
          cur-time (get-current-time)]
      (cond (= param-type :freq) (set-freq-at-time cur-osc value cur-time)
            :else (when (true? on?) (set-gain-at-time cur-gain value cur-time))
            )
      ))
 
(defn init-audio [win cnv]
  (set! ctx (if (.-AudioContext win) (win.AudioContext.) (win.webkitAudioContext.)))
  (doall (map osc-create (keys oscs)))
  (osc-connect :carrier true nil nil)
  ;;(println oscs)
  (fft/init-fft ctx cnv fft-size (get-in oscs [:carrier :gain]))
  )
  
  
