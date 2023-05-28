(ns training.web.view.artist-page.utils)


(defn format-released [^js released]
  (let [y (+ 1900 (.getYear released))
        m (+ 1 (.getMonth released))
        d (.getDate released)]
    (str (when (< d 10) "0") d "."
         (when (< m 10) "0") m "."
         y)))


(defn format-playtime [length]
  (let [hours   (int (/ length 1000.0 60.0 60.0))
        length  (- length (* hours 1000.0 60.0 60.0))
        minutes (int (/ length 1000.0 60.0))
        length  (- length (* minutes 1000.0 60.0))
        seconds (int (/ length 1000.0))]
    (str hours "h "
         (when (< minutes 10) "0")
         minutes "min "
         (when (< seconds 10) "0")
         seconds "sec")))


(defn format-price [price]
  (str (-> price (/ 100.0) (.toFixed 2)) "€"))

