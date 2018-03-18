(ns projects-spec.routes.services.iteration
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [compojure.api.upload :refer [TempFileUpload wrap-multipart-params]]
            [schema.core :as s]
            [clj-time.format :as f]
            [clj-time.coerce :as c]
            [projects-spec.db.core :as db]
            [clojure.java.io :as io]))


(s/defschema Iteration {:id Long
                        :num Long
                        :description String
                        :start_date String
                        :end_date String
                        :project_id Long})

(defn- iteration-default [project-id]
  (let [max-num (:max_num (db/get-max-iter-num {:project_id project-id}))]
    (println "MAX NUM : " max-num)
    {:num (if max-num (inc max-num) 1)
     :description "my iteration simple description"
     :start_date "2018-01-01"
     :end_date "2018-12-31"
     :project_id project-id}))


(defn create-default-iteration [project-id]
  (let [iter (iteration-default project-id)]
    (db/create-iteration! iter)
    (db/get-last-iteration {:project_id project-id})))


(defn delete-iteration [id]
  (db/delete-specs-by-iter! {:iteration_id id})
  (db/delete-iteration! {:id id})
  (ok "iteration deleted."))

(defn save-iteration [iteration]
  (db/update-iteration! iteration)
  (ok "Iteration updated."))

