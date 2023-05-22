(ns training.web.use-atom
  (:require [helix.hooks :as hooks]))


(defn use-atom
  ([atom] (use-atom atom nil))
  ([atom transformers]
   (let [trf               (if transformers
                             (fn [value]
                               (reduce (fn [value f] (f value))
                                       value
                                       transformers))
                             identity)
         [value set-value] (hooks/use-state (trf @atom))]
     (hooks/use-effect
      :once
      (let [key (gensym)]
        (add-watch atom key (fn [_ _ old-atom-value new-atom-value]
                              (let [old-value (trf old-atom-value)
                                    new-value (trf new-atom-value)]
                                (when (not= old-value new-value)
                                  (set-value new-value)))))
        (fn [] (remove-watch atom key))))
     value)))
