(ns projects-spec.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[projects-spec started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[projects-spec has shut down successfully]=-"))
   :middleware identity})
