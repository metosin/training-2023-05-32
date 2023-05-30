(ns training.server.fn-bang)


(deftype FnBang [^{:tag clojure.lang.IFn} this-fn metadata]
  Object
  (toString [_]
    (-> metadata ::fn! :name (pr-str)))
  (hashCode [_]
    (-> metadata ::fn! (hash)))
  (equals [_ that]
    (= (-> metadata ::fn!)
       (-> that (meta) ::fn!)))

  clojure.lang.IMeta
  (meta [_] metadata)

  clojure.lang.Fn

  clojure.lang.IFn
  (applyTo [_this args] (.applyTo this-fn args))
  (invoke [_this] (this-fn))
  (invoke [_this a] (this-fn a))
  (invoke [_this a b] (this-fn a b))
  (invoke [_this a b c] (this-fn a b c))
  (invoke [_this a b c d] (this-fn a b c d))
  (invoke [_this a b c d e] (this-fn a b c d e))
  (invoke [_this a b c d e f] (this-fn a b c d e f)))


(defmacro defn!
  "Like `clojure.core/defn` but saves the arguments list and the function body
   in the metadata of the created function. Differs from `clojure.core/defn` in 
   that this macro does not support multiple arities."
  [fn-name & more]
  (let [[doc-string & more] (if (string? (first more))
                              more
                              (cons nil more))
        [attr-map & more]   (if (map? (first more))
                              more
                              (cons nil more))
        [params & body]     more
        metadata            (merge (meta &form)
                                   attr-map
                                   (when doc-string {:doc doc-string})
                                   {::fn! {:name   `(symbol (name (ns-name ~'*ns*))
                                                            (name (quote ~fn-name)))
                                           :env    nil
                                           :params `(quote ~params)
                                           :body   `(quote ~body)}})]
    `(def ~fn-name
       (->FnBang (fn ~fn-name ~params ~@body)
                 ~metadata))))

(comment

  (macroexpand-1 '(defn! foo [x] (+ x 1)))
  ;; => 
  ;; (def foo (->FnBang (fn foo [x] 
  ;;                      (+ x 1)) 
  ;;                    {:line 56
  ;;                     :column 19
  ;;                     ::fn! {:ns (ns-name *ns*)
  ;;                            :name (symbol (name (ns-name *ns*)) 
  ;;                                          (name (quote foo)))
  ;;                            :env nil
  ;;                            :params (quote [x])
  ;;                            :body (quote ((+ x 1)))}}))
  
  (defn! foo [x]
    (+ x 1))

  (foo 41)
  ;; => 42
  
  (meta foo)
  ;; => 
  ;; {:line    56
  ;;  :column  3
  ;;  ::fn! {:ns     training.server.fn-bang
  ;;            :name   training.server.fn-bang/fn-name
  ;;            :env    nil
  ;;            :params [x]
  ;;            :body   ((+ x 1))}}
  )


(defmacro fn!
  "Like `clojure.core/fn` but saves the environment, arguments list and the function
   body in the metadata of the created function. Differs from `clojure.core/fn` in 
   that this macro does not support multiple arities."
  [& more]
  (let [[fn-name & more]          (if (symbol? (first more))
                                    more
                                    (cons nil more))
        [form-meta params & body] (if (map? (first more))
                                    more
                                    (cons nil more))
        env                       (reduce (fn [acc s]
                                            (assoc acc (list 'quote s) s))
                                          {}
                                          (keys &env))
        metadata                  (merge form-meta
                                         (meta &form)
                                         {::fn! {:name   `(symbol (name (ns-name ~'*ns*))
                                                                  (name (quote ~(if fn-name fn-name 'fn!))))
                                                 :env    env
                                                 :params (list 'quote params)
                                                 :body   (list 'quote body)}})]
    `(->FnBang ~(if fn-name
                  (list* `fn fn-name params body)
                  (list* `fn params body))
               ~metadata)))


(comment

  (macroexpand-1 '(fn! fofo [x y] (* x y)))

  (def bar (fn! [x y] (* x y)))

  (bar 2 21)
  ;; => 42


  (meta bar)
  ;; => 
  ;; {:line    118
  ;;  :column  12
  ;;  ::fn!    {:name   training.server.fn-bang/fn!
  ;;            :env    {}
  ;;            :params [x y]
  ;;            :body   ((* x y))}}

  (def bar
    (let [x 2]
      (fn! [y] (* x y))))

  (bar 21)
  ;; => 42

  (meta bar)
  ;; => 
  ;; {:line                        135
  ;;  :column                      7
  ;;  ::fn! {:name   training.server.fn-bang/fn!
  ;;         :env    {x 2}
  ;;         :params [y]
  ;;         :body   ((* x y))}}

  (meta (fn! boz [x] (+ x 1)))
  ;; => 
  ;; {:line   149
  ;;  :column 9
  ;;  ::fn!   {:name   training.server.fn-bang/boz
  ;;           :env    {}
  ;;           :params [x]
  ;;           :body   ((+ x 1))}}


  (str (fn! [x] (+ x 1)))
  ;; => "training.server.fn-bang/fn!"

  (str (fn! boz [x] (+ x 1)))
  ;; => "training.server.fn-bang/boz"

  )


(defn fn!? [f]
  (contains? (meta f) ::fn!))
