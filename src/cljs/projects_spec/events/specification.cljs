(ns projects-spec.events.specification
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
 :load-specification
 (fn [{db :db} _]
   {:http-xhrio {:method :get
                 :uri "/api/specifications"
                 :format (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:specifications-loaded]
                 :on-failure [:bad-response]}}))

(reg-event-fx
 :specifications-loaded
 (fn [{db :db} [_ response]]
   {:db (-> db
            (assoc :specifications (js->clj response))
            (assoc :completed-specs (->> (js->clj response)
                                         (map #(:implemented %))
                                         (filter true?)
                                         count)))
    :dispatch [:load-stakeholders]}))
