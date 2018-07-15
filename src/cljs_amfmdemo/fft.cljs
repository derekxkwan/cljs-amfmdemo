(ns cljs-amfmdemo.fft
    (:require ))

(def max-val (atom 0))

(def data-array nil)
(def canvas nil)
(def analyzer nil)

(defn set-canvas [cnv] (set! canvas cnv))

(defn create-analyzer [context fft-size]
  (let [anlz (.createAnalyser. context)]
    (set! (.-smoothingTimeConstant anlz) 0.2)
    (set! (.-fftSize anlz) fft-size)
    (set! data-array (js/Uint8Array. (.-frequencyBinCount anlz)))
    (set! analyzer anlz)))

(defn clear-cnv []
   (let [w (.-width canvas)
        h (.-height canvas)
        cnv-ctx (.getContext canvas "2d")]
  (.clearRect. cnv-ctx 0 0 w h)))

(defn handle-stream [context stream]
  (let [ipt (.createMediaStreamSource context stream)]
    (.connect ipt analyzer))
  )

(defn handle-error [error]
  (.log js/console "error obtaining microphone"))

(defn set-mic-input [context]
  (-> (-> js/navigator .-mediaDevices)
      (.getUserMedia #js {:audio true :video false})
      (.then #(handle-stream context %))
      (.catch #(handle-error %))))
  

(defn draw [& {:keys [bg stroke-color] :or {bg "rgb(0,0,0)" stroke-color "rgb(125,75,125)"}}]
  (let [w (.-width canvas)
        h (.-height canvas)
        cnv-ctx (.getContext canvas "2d")
        freq-bin-ct (.-frequencyBinCount analyzer)]
    (.requestAnimationFrame js/window draw)
    (.getByteFrequencyData analyzer data-array)
    (let [cur-max (apply max (array-seq data-array))]
      (when (> cur-max @max-val) (reset! max-val cur-max)))
    (set! (.-fillStyle cnv-ctx) bg)
    (.fillRect. cnv-ctx 0 0 w h)
    (set! (.-lineWidth cnv-ctx) 2)
    (set! (.-strokeStyle cnv-ctx) stroke-color)
    (.beginPath cnv-ctx)
    (let [slice-width (/ w freq-bin-ct)]
      (doall (for [i (range freq-bin-ct)
                   :let [y (* (- 1 (/ (aget data-array i) @max-val)) h)
                         x (* i slice-width)]]
               (if (= i 0)
                 (.moveTo cnv-ctx x y)
                 (.lineTo cnv-ctx x y))
               ))
      (.lineTo cnv-ctx w (/ h 2))
      )
    (.stroke cnv-ctx)
    )
  )
