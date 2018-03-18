(ns projects-spec.events.stakeholder
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

(reg-event-fx
 :load-stakeholders
 (fn [{db :db} _]
   {:http-xhrio {:method :get
                 :uri "/api/stakeholders"
                 :format (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:stakeholders-loaded]
                 :on-failure [:bad-response]}}))

(reg-event-fx
 :stakeholders-loaded
 (fn [{db :db} [_ response]]
   {:db (assoc db :stakeholders (js->clj response))}))
;;subscriptions


