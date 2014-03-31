;; (ns comp.models.korma
;;   ;; (:require [korma.core :refer :all]
;;   ;;           [korma.db :refer :all])
;;   )

;; (defrecord Entity [database])

;; (defmacro defentity
;;   "Define an entity representing a table in the database, applying any
;;   modifications in the body."
;;   [ent & body]
;;   `(let [e# (-> (create-entity ~(name ent))
;;                 ~@body)]
;;      (def ~ent e#)))

;; (defn make-entity
;;   )
