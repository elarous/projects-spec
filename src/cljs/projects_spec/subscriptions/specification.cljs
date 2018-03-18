(ns projects-spec.subscriptions.specification
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


(reg-sub
 :iteration-specs
 (fn [db [_ id]]
   (get-in db [:specs-by-iteration id])))

(reg-sub
 :completed-specs
 (fn [db [_]]
   (get-in db [:completed-specs])))



