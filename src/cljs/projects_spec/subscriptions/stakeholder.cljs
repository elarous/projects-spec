(ns projects-spec.subscriptions.stakeholder
  (:require [projects-spec.db :as db]
            [ajax.core :as ajax]
            [clojure.string :as s]
            [day8.re-frame.http-fx]
            [re-frame.core :refer [dispatch
                                   reg-event-db
                                   reg-event-fx
                                   reg-sub
                                   ->interceptor
                                   path
                                   debug]
             :as rf]
            [projects-spec.events.project :refer [project-by-id
                                                  project-name]]
            [projects-spec.events.iteration :refer [iter-by-id
                                                    iter-num]]))

