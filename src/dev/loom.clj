(ns loom
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log])
  (:import (java.util.concurrent Executors)
           (jdk.incubator.concurrent StructuredTaskScope
                                     StructuredTaskScope$ShutdownOnFailure
                                     StructuredTaskScope$ShutdownOnSuccess
                                     ScopedValue)))


(set! *warn-on-reflection* true)


;;
;; Some utils:
;;


(defn logger [task-name]
  (fn [& args]
    (log/info (format "task: [%s] - %s" task-name (str/join " " args)))))


(defn make-task ^Runnable [task-name success?]
  (fn []
    (let [log   (logger task-name)
          delay (-> (rand) (* 2000) (+ 1000) (long))]
      (log "Starting, delay is" delay)
      (try
        (Thread/sleep delay)
        (catch InterruptedException e
          (log "Interrupted")
          (throw e)))
      (when-not success?
        (log "Throwing...")
        (throw (ex-info (format "task %s failed" task-name) {})))
      (log "Ready")
      task-name)))


(defn ->Function ^java.util.function.Function [f]
  (reify java.util.function.Function
    (apply [_this v]
      (f v))))


;; JEP 437: Structured Concurrency (Second Incubator)
;; https://openjdk.org/jeps/437
;; - Write correct code that herds flocks of concurrent tasks
;; - Cancellation, error propagation, timeouts
;; - Observe hierarchy at runtime

(comment

  ;
  ; ShutdownOnSuccess scope: Race tasks
  ; - first to complete -> scope done
  ; - cancel slower tasks
  ; - provides the result of the fastets task
  ;
  ; Usefull when you have multiple implementations of a task but you only
  ; need one of them to succeed. Either for speed (fastest task) or redundancy
  ; (at least one task succeeds).
  ;

  (do (log/info "-- ShutdownOnSuccess: -----------------------")
      (let [result (with-open [scope (StructuredTaskScope$ShutdownOnSuccess.)]
                     (doto scope
                       (.fork (make-task "1" true))
                       (.fork (make-task "2" true))
                       (.fork (make-task "3" true)))
                     (.join scope)
                     (.result scope))]
        (log/info "ShutdownOnSuccess: successful:" result))
      (log/info "------------------------------------------------------"))

  ;
  ; ShutdownOnFailure scope: All are needed
  ; - first to fail -> scope done
  ; - cancel all tasks
  ; - throws the failed task exception
  ; - no special handling of successfull case
  ;
  ; Usefull when you have multiple steps that can be done concurrently.
  ;

  (do (log/info "-- ShutdownOnFailure: -----------------------")
      (let [result (with-open [scope (StructuredTaskScope$ShutdownOnFailure.)]
                     (let [tasks (->> [["1" true]
                                       ["2" true]
                                       ["3" true]]
                                      (map (fn [[task-name succeed?]] (.fork scope (make-task task-name succeed?))))
                                      (doall))]
                       (.join scope)
                       (.throwIfFailed scope (->Function (fn [e] (ex-info "Task failed" {} e))))
                       (mapv deref tasks)))]
        (log/info "ShutdownOnFailure: successful:" result))
      (log/info "------------------------------------------------------"))

  ;
  )



;; JEP 436: Virtual Threads (Second Preview)
;; https://openjdk.org/jeps/436
;; - Lots and lots of threads
;;
;; https://ales.rocks/notes-on-virtual-threads-and-clojure
;; https://docs.oracle.com/en/java/javase/20/core/virtual-threads.html
;; https://blogs.oracle.com/javamagazine/post/java-loom-virtual-threads-platform-threads


(comment

  (.start (Thread/ofVirtual) (fn [] (println "This is"
                                             (if (.isVirtual (Thread/currentThread))
                                               "Virtual"
                                               "Platform")
                                             "thread")))


  (.start (Thread/ofPlatform) (fn [] (println "This is"
                                              (if (.isVirtual (Thread/currentThread))
                                                "Virtual"
                                                "Platform")
                                              "thread")))


  (do (log/info "-- Virtual threads executor: Start -----------------------")
      (with-open [ex (Executors/newVirtualThreadPerTaskExecutor)]
        (.submit ex (make-task "1" false))
        (.submit ex (make-task "2" true))
        (.submit ex (make-task "3" false)))
      (log/info "-- Virtual threads executor: Done ------------------------"))


  (do (log/info "-- Virtual threads executor: Start -----------------------")
      (let [start (System/currentTimeMillis)]
        (with-open [ex (Executors/newVirtualThreadPerTaskExecutor)]
          (->> (repeat 1000000 (fn [] (Thread/sleep 1000)))
               (map (fn [^Runnable task] (.submit ex task)))
               (dorun)))
        (let [end (System/currentTimeMillis)]
          (log/info "-- Virtual threads executor: Done ------------------------")
          (log/info "-- Duration" (- end start) "ms"))))


  (do (log/info "-- Platform threads executor: Start -----------------------")
      (let [start (System/currentTimeMillis)]
        (with-open [ex (Executors/newThreadPerTaskExecutor (.factory (Thread/ofPlatform)))]
          (->> (repeat 10000 (fn [] (Thread/sleep 1000)))
               (map (fn [^Runnable task] (.submit ex task)))
               (dorun)))
        (let [end (System/currentTimeMillis)]
          (log/info "-- Platform threads executor: Done ------------------------")
          (log/info "-- Duration" (- end start) "ms"))))

  ;
  )



;; JEP 429: Scoped Values (Incubator)
;; https://openjdk.org/jeps/429
;; - Sharing of data within and across threads
;; - Light-weight option for Thread locals
;; - Immutable
;; - Propably not very usefull for clojurians


(comment

  (def scoped-value (ScopedValue/newInstance))

  (defn some-task []
    (log/info "same-task: Value is" (.get scoped-value)))

  (ScopedValue/where scoped-value "Hello" (fn []
                                            (with-open [scope (StructuredTaskScope.)]
                                              (.fork scope ^Runnable some-task)
                                              (.join scope))))

  ;
  )
