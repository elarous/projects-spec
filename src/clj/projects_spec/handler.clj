(ns projects-spec.handler
  (:require [compojure.core :refer [routes wrap-routes]]
            [projects-spec.layout :refer [error-page]]
            [projects-spec.routes.home :refer [home-routes]]
            [projects-spec.routes.services :refer [service-routes]]
            [compojure.route :as route]
            [projects-spec.env :refer [defaults]]
            [mount.core :as mount]
            [projects-spec.middleware :as middleware]))

(mount/defstate init-app
  :start ((or (:init defaults) identity))
  :stop  ((or (:stop defaults) identity)))

(mount/defstate app
  :start
  (middleware/wrap-base
    (routes
      (-> #'home-routes
          (wrap-routes middleware/wrap-csrf)
          (wrap-routes middleware/wrap-formats))
          #'service-routes
      (route/not-found
        (:body
          (error-page {:status 404
                       :title "page not found"}))))))
