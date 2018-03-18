(ns projects-spec.events.home
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

(defn testing [] 
	(str "ok"))

(reg-event-db
 :initialize-db
 (fn [_ _]
   db/default-db))

(reg-event-db
 :set-active-page
 (fn [db [_ page]]
   (assoc db :page page)))
