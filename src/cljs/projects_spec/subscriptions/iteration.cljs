(ns projects-spec.subscriptions.iteration
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
                                                    iter-num
                                                    iters-by-project]]))


(reg-sub
 :project-iterations
 (fn [db [_ project-id]]
   (sort-by :num (iters-by-project (:iterations db) project-id))))


(reg-sub
 :iteration-start-date
 (fn [db [_ id]]
   (:start_date (iter-by-id (:iterations db) id))))

(reg-sub
 :iteration-end-date
 (fn [db [_ id]]
   (:end_date (iter-by-id (:iterations db) id))))

(reg-sub
 :iteration-days-left
 (fn [db [_ id]]
   (:days-left (iter-by-id (:iterations db) id))))

(reg-sub
 :iteration-days-passed
 (fn [db [_ id]]
   (:days-passed (iter-by-id (:iterations db) id))))

(reg-sub
 :iteration-days-modal
 (fn [db [_ id]]
   (get-in db [:iteration-days-modal id])))

(reg-sub
 :iteration-desc-editing
 (fn [db [_ id]]
   (get-in db [:iteration-desc-editing id])))

(reg-sub
 :iteration-desc
 (fn [db [_ id]]
   (:description (iter-by-id (:iterations db) id))))

(reg-sub
 :iteration-num
 (fn [db [_ id]]
   (:num (iter-by-id (:iterations db) id))))


