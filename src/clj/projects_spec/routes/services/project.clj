(ns projects-spec.routes.services.project
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [compojure.api.upload :refer [TempFileUpload wrap-multipart-params]]
            [schema.core :as s]
            [clj-time.format :as f]
            [clj-time.coerce :as c]
            [projects-spec.db.core :as db]
            [clojure.java.io :as io]))



(s/defschema Project {:id Long
                      :name String
                      :description String
                      :start_date String
                      :end_date String
                      :img_src String
                      :completed Boolean})


(def default-project {:name "New Project"
                      :description "New Project Description"
                      :start_date "2018-01-01"
                      :end_date "2018-12-31"
                      :img_src "new_project.png" :completed false})


(defn update-project-img [project-id file]
  (let [filename (:filename file)]
    (db/update-project-img {:id project-id
                            :img_src filename})
    (io/copy (:tempfile file) (io/file (str "resources/public/img/" filename)))
    (ok "project image updated !")))


(defn delete-project [id]
  (db/delete-iters-by-project! {:project_id id})
  (db/delete-project! {:id id})
  (ok {:id id}))


(defn save-project [project]
  (db/update-project! {:id (:id project)
                       :name (:name project)
                       :description (:description project)
                       :start_date (:start_date project)
                       :end_date (:end_date project)
                       :completed (:completed project)
                       :img_src (:img_src project)})
  (ok "Project updated."))


(defn create-default-project []
  (db/create-project! default-project)
  (db/get-last-project))

