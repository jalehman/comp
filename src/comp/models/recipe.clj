(ns comp.models.recipe
  (:refer-clojure :exclude [distinct group-by])
  (:require [comp.models.common :refer :all]
            [comp.database :refer [query]]
            [clojure.java.jdbc :as j]
            [sqlingvo.core :refer :all]
            [com.stuartsierra.component :as component]))

(comment

  (def conn (get-in user/system [:database :connection]))

  (defn one
    [conn conditions & [fields]]
    ((one* conn :user_accounts) conditions fields))

  (= (comp.database/query
      conn
      (select [*]
              (from :recipes)
              (where '(= :recipes.id "5160757b96cc6207a37ff77a"))
              (join :ingredient-lines `(on (= :ingredient-lines.recipes-id :recipes.id)))))

     (comp.database/query
      conn
      (select [*]
              (from :recipes)
              (where '(= :recipes.id "5160757b96cc6207a37ff77a"))
              (join :ingredient-lines.recipes-id :recipes.id)))

     (comp.database/query
      conn
      (select [*]
              (from (as :recipes :r))
              (where '(= :r.id "5160757b96cc6207a37ff77a"))
              (join :ingredient-lines '(on (= :ingredient-lines.recipes-id :r.id)))))

     (comp.database/query
      conn
      (select [*]
              (from :recipes)
              (where '(= :recipes.id "5160757b96cc6207a37ff77a")))))

  (let [recipe (first
                (comp.database/query
                 conn
                 (select [*]
                         (from :recipes)
                         (where '(= :recipes.id "5160757b96cc6207a37ff77a")))))
        lines (comp.database/query
               conn
               (select [*]
                       (from :ingredient-lines)
                       (where '(= :recipes-id "5160757b96cc6207a37ff77a"))))]
    (assoc recipe :ingredient-lines lines))

  (let [recipe ((one* conn :recipes) {:id "5160757b96cc6207a37ff77a"})
        lines((many* conn :ingredient-lines) {:recipes-id "5160757b96cc6207a37ff77a"})]
    (assoc recipe :ingredient-lines lines))

  )

(defrecord Recipe [database])

(defmacro base* [fs & body]
  `(fn [conditions#]
     (select ~fs
             (when-not (empty? conditions#)
               (where (map->exprs conditions#)))
             ~@body)))

(comment

  (def conn (get-in user/system [:database :connection]))

  (defn parse-fields [fields]
    (letfn [(parse-fields* []
              (for [f fields]
                (if (vector? f)
                  (sqlingvo.core/as (first f) (second f))
                  f)))]
      (cond
       (vector? fields) (parse-fields*)
       (map? fields)    (let [{:keys [fields literal?]} fields]
                          (if literal? fields (parse-fields*)))
       :otherwise       [*])))

  (defmacro parse-conditions* [conditions]
    `(let [exprs# (for [[k# v#] ~conditions]
                    `(= ~k# ~v#))]
       (if (> (count exprs#) 1)
         (conj exprs# 'clojure.core/and)
         (first exprs#))))

  (defn parse-conditions [conditions]
    (cond
     (empty? conditions) nil
     (list? conditions)  (where conditions)
     (map?  conditions)  (where (parse-conditions* conditions))
     :otherwise nil))

  (defn query-builder
    [& {:keys [table conditions fields limit raw?]
        :or {fields [*], raw? false}
        :as attrs}]
    (assert (contains? attrs :table))
    (let [s (select (parse-fields fields)
                    (from table)
                    (parse-conditions conditions)
                    (sqlingvo.core/limit limit))]
      (if raw? s (query conn s))))

  (defn related [ms]
    (for [m ms]
      ))

  (query-builder
   :table :recipes
   :conditions {:source "101cookbooks"}
   :fields [:* [:name :title]]
   :limit 2
   :raw? false)

  (query-builder
   :table :recipe-cuisines
   :limit 2)

  (query-builder
   :table :recipes
   :conditions {:source "101cookbooks"}
   :fields [:* [:name :title]]
   :related {:table :cuisines}
   :limit 2
   :raw? false)

  (defmacro defquery [name & kvs]
    (if (vector? (first kvs))
      (let [args (first kvs)
            kvs  (rest kvs)]
        `(defn ~name [~@args]
           (query-builder ~@kvs)))))

  (defquery recipes [limit]
    :table :recipes
    :conditions {:source "101cookbooks"}
    :fields [:* [:name :title]]
    :limit limit)

  ((base* [*]
          (from :recipes)
          (join :recipe-images.recipes-id :recipes.id ))
   {}))

(defn one [recipe conditions])

;; (defn recipe []
;;   (map->Recipe {}))

;; (defn one [recipe conditions]
;;   (let [{:keys [entity]} recipe]
;;     (sql-only
;;      (select entity
;;              (where conditions)
;;              (with ingredient-lines)
;;              (limit 1)))))

;; (comment (one (:recipe user/system) {:prep_time 0}))
