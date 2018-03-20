(ns projects-spec.routes.services.stakeholder
  (:require [schema.core :as s]
            [ring.util.http-response :refer :all]
            [projects-spec.db.core :as db]
            [compojure.api.sweet :refer :all]))


(s/defschema Stakeholder {:id Long
                          :name String
                          :description String
                          :project_id Long
                          :fg_color String
                          :bg_color String})

(def default-stakeholder {:name "Stakeholder Name"
                          :description "Stakeholder Description"
                          :fg_color "#000000"
                          :bg_color "#ffffff"})

(defn- make-default [project-id]
  (assoc default-stakeholder :project_id project-id))

(defn save-stakeholder! [stakeholder]
  (db/update-stakeholder! stakeholder)
  (ok "Stakeholder updated"))

(defn create-stakeholder! [project-id]
  (db/create-stakeholder! (make-default project-id))
  (ok (db/get-last-stakeholder {:project_id project-id})))

(defn delete-stakeholder! [id]
  (db/delete-stakeholder! {:id id})
  (ok "stakeholder deleted"))

