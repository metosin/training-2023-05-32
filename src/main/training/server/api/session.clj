(ns training.server.api.session)


(defn get-session
  ([req]
   (get req ::session))
  ([req key]
   (get-in req [::session key])))
