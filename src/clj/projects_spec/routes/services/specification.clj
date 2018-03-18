(ns projects-spec.routes.services.specification
  (:require [schema.core :as s]))


(s/defschema Specification {:id Long
                            :reason String
                            :spec String
                            :stakeholder_id Long
                            :iteration_id Long
                            :implemented Boolean})
