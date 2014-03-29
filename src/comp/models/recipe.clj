(ns comp.models.recipe
  (:require [korma.core :as k :refer :all :exclude [defentity]]
            [com.stuartsierra.component :as component]))

(defmacro defentity
  "A version of defentity that returns a function that takes the
   database as an argument, rather than a static Var."
  [ent & body]
  (let [db (gensym)]
    `(defn ~ent [~db]
       (-> (create-entity ~(name ent))
           (k/database (:connection ~db))
           ~@body))))

;; (defentity ingredient-lines
;;   (table :ingredient_lines))

;; (defentity recipes
;;   (has-many ingredient-lines))

(defrecord Recipe [database]
  component/Lifecycle
  (start [component]
    (let [recipe-entity (-> (create-entity "recipes")
                            (k/database (:connection database))
                            (has-many (-> (create-entity "ingredient-lines")
                                          (k/database (:connection database))
                                          (table :ingredient_lines))))]
      (println ";; Initializing recipes entity")
      (assoc component :entity recipe-entity)))
  (stop [component]
    (println ";; Removing recipes entity")
    (dissoc component :entity)))

(defn recipe []
  (map->Recipe {}))

(defn one [recipe conditions]
  (let [{:keys [entity]} recipe]
    (sql-only
     (select entity
             (where conditions)
             (with ingredient-lines)
             (limit 1)))))

(comment (one (:recipe user/system) {:prep_time 0}))
