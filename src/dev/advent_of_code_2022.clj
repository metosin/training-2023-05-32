(ns advent-of-code-2022)


; https://adventofcode.com/2022/day/1


(defn make-random [seed]
  (let [random (java.util.Random. seed)]
    (fn
      ([] (abs (.nextInt random)))
      ([bound] (abs (.nextInt random (int bound)))))))


(comment
  (let [r (make-random 1337)]
    [(r 100) (r 100) (r 100)])
  ;; => [21 44 59] 
  )


(defn make-elf-report [random]
  (let [number-of-lines (inc (random 10))]
    (concat (repeatedly number-of-lines
                        (partial random 10000))
            ['EOF])))


(comment
  (make-elf-report (make-random 1234))
  ;; => (4633 5133 220 7210 6393 8529 7449 2297 1037 EOF) 
  )


(defn elf-reports [random]
  (let [reports (fn reports [[next-line & more-lines]]
                  (lazy-seq
                   (if next-line
                     (cons next-line (reports more-lines))
                     (reports (make-elf-report random)))))]
    (reports (make-elf-report random))))


(comment
  (take 30 (elf-reports (make-random 1234)))
  ;; => (4633 5133 220 7210 6393 8529 7449 2297 1037 EOF 
  ;;     5038 EOF 
  ;;     6286 1889 364 5450 8012 4897 4927 974 EOF 
  ;;     1907 7946 4178 2363 4072 1696 1989 5806 2218) 
  )


(defn get-best-elf [reports]
  ; TODO...
  )


(comment
  (get-best-elf [1000
                 2000
                 3000
                 'EOF
                 4000
                 'EOF
                 5000
                 6000
                 'EOF
                 7000
                 8000
                 9000
                 'EOF
                 10000])
  ;; => [24000 4]


  (->> (elf-reports (make-random 1234))
       (take 1000000)
       (get-best-elf))
  ;; => [84535 24754]
  )