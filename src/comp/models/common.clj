(ns comp.models.common
  (:refer-clojure :exclude [distinct group-by])
  (:require [comp.database :refer [query execute!]]
            [sqlingvo.core :refer :all]
            [clj-time.core :as ctime]
            [clj-time.coerce :as coerce]))

;; =============================================================================
;; Util

(defn sql-current-time
  "Current timestamp in SQL Timestamp format"
  [& {:keys [add]}]
  (java.sql.Timestamp. ^long (coerce/to-long
                              (ctime/plus (ctime/now) (ctime/days (or add 0))))))

(defmacro map->exprs [conditions]
  `(let [exprs# (for [[k# v#] ~conditions]
                  `(= ~k# ~v#))]
     (if (> (count exprs#) 1)
       (conj exprs# 'clojure.core/and)
       (first exprs#))))

(defn- assoc-timestamps
  [r]
  (let [current-time (sql-current-time)]
    (merge r {:created_at current-time
              :updated_at current-time})))

;; =============================================================================
;; Common function generators

;; There's some code duplication between these two. Use 'one' as a
;; special case of 'many'?
(defmacro many*
  [conn table]
  `(fn [conditions# & ks#]
     (let [ks# (remove nil? ks#)
           fs# (if-not (empty? ks#) ks# [*])]
       (query ~conn
              (select fs#
                      (from ~table)
                      (when-not (empty? conditions#)
                        (where (map->exprs conditions#))))))))

(defmacro one*
  [conn table]
  `(fn [conditions# & ks#]
     (let [ks# (remove nil? ks#)
           fs# (if-not (empty? ks#) ks# [*])]
       (-> (query ~conn
                  (select fs#
                          (from ~table)
                          (when-not (empty? conditions#)
                            (where (map->exprs conditions#)))
                          (limit 1)))
           first))))

(defn delete!*
  [conn table conditions]
  (execute! ~conn (delete table
                         (where (map->exprs conditions)))))

(defmulti create!* (fn [_ _ r] (class r)))

(defmethod create!* clojure.lang.PersistentArrayMap [_ table r]
  (create!* table [r]))

(defmethod create!* clojure.lang.Sequential [conn table rs]
  (let [records (map assoc-timestamps rs)]
    (execute! ~conn (insert table []
                           (values records)))))
