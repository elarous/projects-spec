(ns user
  (:require [luminus-migrations.core :as migrations]
            [projects-spec.config :refer [env]]
            [mount.core :as mount]
            [projects-spec.figwheel :refer [start-fw stop-fw cljs]]
            [projects-spec.core :refer [start-app]]))

(defn start []
  (mount/start-without #'projects-spec.core/repl-server))

(defn stop []
  (mount/stop-except #'projects-spec.core/repl-server))

(defn restart []
  (stop)
  (start))

(defn reset-db []
  (migrations/migrate ["reset"] {:database-url (:config-database-url env)}))

(defn migrate []
  (migrations/migrate ["migrate"] (select-keys env [:database-url])))

(defn rollback []
  (migrations/migrate ["rollback"] (select-keys env [:database-url])))

(defn create-migration [name]
  (migrations/create name (select-keys env [:database-url])))


