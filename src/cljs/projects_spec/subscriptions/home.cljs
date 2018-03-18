(ns projects-spec.subscriptions.home
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

(defn testing []
  (println "testing .... "))

(reg-sub
 :page
 (fn [db _]
   (:page db)))

(reg-sub
 :notif-type
 (fn [db [_ id]]
   (get-in db [:notif :notif-type])))

(reg-sub
 :notif-content
 (fn [db [_ id]]
   (get-in db [:notif :notif-content])))

(reg-sub
 :notif-visible
 (fn [db [_ id]]
   (get-in db [:notif :visible])))
