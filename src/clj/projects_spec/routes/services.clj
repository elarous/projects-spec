(ns projects-spec.routes.services
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [compojure.api.upload :refer [TempFileUpload wrap-multipart-params]]
            [schema.core :as s]
            [clj-time.format :as f]
            [clj-time.coerce :as c]
            [projects-spec.db.core :as db]
            [clojure.java.io :as io])
  (:import org.joda.time.DateTime))

;; TODO : when removing a project iterations should be removed first
;; dues to the foreign keys constraints ! 


(s/defschema Project {:id Long
                      :name String
                      :description String
                      :start_date String
                      :end_date String
                      :img_src String
                      :completed Boolean})

(s/defschema Iteration {:id Long
                        :num Long
                        :description String
                        :start_date String
                        :end_date String
                        :project_id Long})

(s/defschema Stakeholder {:id Long
                          :name String
                          :description String})

(s/defschema Specification {:id Long
                            :reason String
                            :spec String
                            :stakeholder_id Long
                            :iteration_id Long
                            :implemented Boolean})

(def default-project {:name "New Project"
                      :description "New Project Description"
                      :start_date "2018-01-01"
                      :end_date "2018-12-31"
                      :img_src "new_project.png" :completed false})

(defn- iteration-default [project-id]
  (let [max-num (:max_num (db/get-max-iter-num {:project_id project-id}))]
    (println "MAX NUM : " max-num)
    {:num (if max-num (inc max-num) 1)
     :description "my iteration simple description"
     :start_date "2018-01-01"
     :end_date "2018-12-31"
     :project_id project-id}))

(defn format-date [num]
  (f/unparse (f/formatters :date)
             (c/from-long num)))

(defn map-format-date [m]
  (-> (update m :start_date format-date)
      (update :end_date format-date)))

(defn create-default-project []
  (db/create-project! default-project)
  (db/get-last-project))

(defn update-project-img [project-id file]
  (let [filename (:filename file)]
    (db/update-project-img {:id project-id
                            :img_src filename})
    (io/copy (:tempfile file) (io/file (str "resources/public/img/" filename)))
    (ok "project image updated !")))

(defn create-default-iteration [project-id]
  (let [iter (iteration-default project-id)]
    (db/create-iteration! iter)
    (db/get-last-iteration {:project_id project-id})))

(defn delete-project [id]
  (db/delete-iters-by-project! {:project_id id})
  (db/delete-project! {:id id})
  (ok {:id id}))

(defn delete-iteration [id]
  (db/delete-specs-by-iter! {:iteration_id id})
  (db/delete-iteration! {:id id})
  (ok "iteration deleted."))

(defn save-project [project]
  (db/update-project! {:id (:id project)
                       :name (:name project)
                       :description (:description project)
                       :start_date (:start_date project)
                       :end_date (:end_date project)
                       :completed (:completed project)
                       :img_src (:img_src project)})
  (ok "Project updated."))

(defn save-iteration [iteration]
  (db/update-iteration! iteration)
  (ok "Iteration updated."))

(defapi service-routes
  {:swagger {:ui "/swagger-ui"
             :spec "/swagger.json"
             :data {:info {:version "1.0.0"
                           :title "Sample API"
                           :description "Sample Services"}}}}
  (context "/api" []
           (GET "/hello/:name" []
                :return String
                :path-params [name :- String]
                :summary "Say hello to the given name"
                (ok (str "Hi, " name " !")))

           (GET "/project/:id" []
                :return Project
                :path-params [id :- Long]
                :summary "get the project given id"
                (ok (map-format-date (db/get-project {:id id}))))

           (POST "/project/create" []
                 :return Project
                 :summary "create and return a new project"
                 (ok (map-format-date
                      (create-default-project))))

           (POST "/project/delete" []
                 :return {:id Long}
                 :body-params [id :- Long]
                 :summary "delete the project with the given id"
                 (delete-project id))

           (POST "/project/save" []
                 :body-params [project :- Project]
                 :summary "Save the modified project"
                 (save-project project))

           (POST "/project/update-img" []
                 :multipart-params [project-id :- s/Int
                                    file :- TempFileUpload]
                 :middleware [wrap-multipart-params]
                 :summary "update the project image"
                 (update-project-img project-id file))

           (GET "/projects" []
                :return [Project]
                :path-params []
                :summary "get all projects"
                (ok (map map-format-date (db/get-all-projects))))


           (GET "/iteration/:id" []
                :return Iteration
                :path-params [id :- Long]
                :summary "get the iteration given id"
                (ok (map-format-date (db/get-iteration {:id id}))))

           (POST "/iteration/create" []
                 :return Iteration
                 :body-params [project-id :- Long]
                 :summary "create new iteration for given project"
                 (ok (-> (create-default-iteration project-id)
                         (map-format-date))))

           (POST "/iteration/save" []
                 :body-params [iteration :- Iteration]
                 :summary "Save the modified iteration"
                 (save-iteration iteration))

           (POST "/iteration/delete" []
                 :body-params [id :- Long]
                 :summary "delete the iteration with the given id"
                 (delete-iteration id))

           (GET "/iterations" []
                :return [Iteration]
                :path-params []
                :summary "get all iterations"
                (ok (map map-format-date (db/get-all-iterations))))

           (GET "/specification/:id" []
                :return Specification
                :path-params [id :- Long]
                :summary "get the specification given id"
                (ok (db/get-spec {:id id})))

           (GET "/specifications" []
                :return [Specification]
                :path-params []
                :summary "get all the specifications"
                (ok (db/get-all-specs)))

           (GET "/stakeholder/:id" []
                :return Stakeholder
                :path-params [id :- Long]
                :summary "get the stakeholder given id"
                (ok (db/get-stakeholder {:id id})))

           (GET "/stakeholders" []
                :return [Stakeholder]
                :path-params []
                :summary "get all specifications"
                (ok (db/get-all-stakeholders)))))



