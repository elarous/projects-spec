(ns ^:figwheel-load projects-spec.subscriptions.stakeholder
  (:require [projects-spec.db :as db]
            [ajax.core :as ajax]
            [clojure.string :as s]
            [day8.re-frame.http-fx]
            [re-frame.core :refer [reg-sub] :as rf]
            [projects-spec.events.stakeholder :refer [stakeholder-by-id]]))

(reg-sub
 :project-stakeholders
 (fn [db [_ id]]
   (->> (:stakeholders db)
        (filter #(= (:project_id %) id))
        (sort-by :id))))

;; move to specification subscriptions
(reg-sub
 :stakeholder-specs
 (fn [db [_ id]]
   (->> (:specifications db)
        (filter #(= (:stakeholder_id %) id)))))

;; move to specification subscriptions
(reg-sub
 :stakeholder-specs-count
 (fn [db [_ id]]
   (->> (:specifications db)
        (filter #(= (:stakeholder_id %) id))
        count)))

(reg-sub
 :stakeholder-desc
 (fn [db [_ id]]
   (:description (stakeholder-by-id (:stakeholders db) id))))

(reg-sub
 :stakeholder-name
 (fn [db [_ id]]
   (:name (stakeholder-by-id (:stakeholders db) id))))

(reg-sub
 :stakeholder-fg
 (fn [db [_ id]]
   (:fg_color (stakeholder-by-id (:stakeholders db) id))))

(reg-sub
 :stakeholder-bg
 (fn [db [_ id]]
   (:bg_color (stakeholder-by-id (:stakeholders db) id))))

(reg-sub
 :stakeholder-name-editing
 (fn [db [_ id]]
   (get-in db [:stakeholder-name-editing id])))

(reg-sub
 :stakeholder-desc-editing
 (fn [db [_ id]]
   (get-in db [:stakeholder-desc-editing id])))
