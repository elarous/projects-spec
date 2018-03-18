(ns projects-spec.routes.services
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [compojure.api.upload :refer [TempFileUpload wrap-multipart-params]]
            [schema.core :as s]
            [clj-time.format :as f]
            [clj-time.coerce :as c]
            [projects-spec.db.core :as db]
            [clojure.java.io :as io]
            [projects-spec.routes.services.project :refer :all ]
            [projects-spec.routes.services.iteration :refer :all]
            [projects-spec.routes.services.util :refer :all]
            [projects-spec.routes.services.specification :refer :all]
            [projects-spec.routes.services.stakeholder :refer :all])
  (:import org.joda.time.DateTime))

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



