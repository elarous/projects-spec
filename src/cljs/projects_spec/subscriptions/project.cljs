(ns projects-spec.subscriptions.project
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
 :projects
 (fn [db _]
   (sort-by :id (:projects db))))

(reg-sub
 :project
 (fn [db _]
   (:active-project db)))

(reg-sub
 :project-view
 (fn [db _]
   (:viewing-project db)))


(reg-sub
 :project-days-modal
 (fn [db [_ id]]
   (get-in db [:project-days-modal id])))

(reg-sub
 :project-start-date
 (fn [db [_ id]]
   (:start_date (project-by-id (:projects db) id))))

(reg-sub
 :project-end-date
 (fn [db [_ id]]
   (:end_date (project-by-id (:projects db) id))))

(reg-sub
 :project-days-left
 (fn [db [_ id]]
   (:days-left (project-by-id (:projects db) id))))

(reg-sub
 :project-days-passed
 (fn [db [_ id]]
   (:days-passed (project-by-id (:projects db) id))))

(reg-sub
 :project-name-editing
 (fn [db [_]]
   (:project-name-editing db)))

(reg-sub
 :project-desc-editing
 (fn [db [_]]
   (:project-desc-editing db)))

(reg-sub
 :project-name
 (fn [db [_ id]]
   (:name (project-by-id (:projects db) id))))

(reg-sub
 :project-desc
 (fn [db [_ id]]
   (:description (project-by-id (:projects db) id))))


(reg-sub
 :project-img
 (fn [db [_ id]]
   (:img_src (project-by-id (:projects db) id))))
