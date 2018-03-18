(ns projects-spec.routes.services.util
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [compojure.api.upload :refer [TempFileUpload wrap-multipart-params]]
            [schema.core :as s]
            [clj-time.format :as f]
            [clj-time.coerce :as c]
            [projects-spec.db.core :as db]
            [clojure.java.io :as io]
            [projects-spec.routes.services.project :refer :all ]
            [projects-spec.routes.services.iteration :refer :all]))

(defn format-date [num]
  (f/unparse (f/formatters :date)
             (c/from-long num)))

(defn map-format-date [m]
  (-> (update m :start_date format-date)
      (update :end_date format-date)))

