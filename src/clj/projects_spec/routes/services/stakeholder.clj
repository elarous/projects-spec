(ns projects-spec.routes.services.stakeholder
  (:require [schema.core :as s]))


(s/defschema Stakeholder {:id Long
                          :name String
                          :description String})
