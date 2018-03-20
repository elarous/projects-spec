(ns projects-spec.events.common
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
             :as rf]))

(defn update-coll-item-by-id [coll id k new-val]
  (let [new-item (assoc (->> coll
                             (some #(and (= (:id %) id) %)))
                        k new-val)]
    (conj (remove #(= (:id %) id) coll)
          new-item)))

(defn millis->days [millis]
  (.ceil js/Math (quot millis 86400000)))

(reg-event-db
 :bad-response
 (fn [db [_ response]]
   (js/alert response)
   db))


(reg-event-db
 :notify
 (fn [db [_ notif-type notif-content]]
   (-> db
       (assoc-in [:notif :animate] true)
       (assoc-in [:notif :notif-type] notif-type)
       (assoc-in [:notif :notif-content] notif-content)
       (assoc-in [:notif :visible] true))))


(reg-event-db
 :show-notif
 (fn [db [_]]
   (assoc-in db [:notif :visible] true)))

(reg-event-db
 :hide-notif
 (fn [db [_]]
   (assoc-in db [:notif :visible] false)))

(reg-event-db
 :toggle-notif
 (fn [db [_]]
   (update-in db [:notif :visible] #(not %))))

