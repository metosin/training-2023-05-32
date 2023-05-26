(ns training.server.util
  (:import (java.util.zip Adler32)))


(set! *warn-on-reflection* true)


(defn checksum [& args]
  (let [adler (Adler32.)]
    (doseq [v args]
      (.update adler (.getBytes (str v))))
    (str (.getValue adler))))


(defn checksummer []
  (let [adler (Adler32.)]
    (fn checksum
      ([data]
       (.update adler (.getBytes (str data)))
       checksum)
      ([] (str (.getValue adler))))))


(comment

  (checksum "hello" "world")
  ;; => "389415997"


  (let [c (checksummer)]
    (c "hello")
    (c "world")
    (c))
  ;; => "389415997" 
  )