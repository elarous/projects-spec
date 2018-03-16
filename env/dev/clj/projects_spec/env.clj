(ns projects-spec.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [projects-spec.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[projects-spec started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[projects-spec has shut down successfully]=-"))
   :middleware wrap-dev})
