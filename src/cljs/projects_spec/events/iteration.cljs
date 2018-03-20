(ns projects-spec.events.iteration
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
            [projects-spec.events.common :refer [update-coll-item-by-id
                                                 millis->days]]))


(defn iter-by-id [iterations id]
  (some #(and (= (:id %) id) %) iterations))

(defn iter-num [db id]
  (:name (iter-by-id (:iterations db) id)))

(defn iters-by-project [iterations project-id]
  (filter #(and (= (:project_id %) project-id) %) iterations))


;; interceptors
(defn- add-date [iteration]
  (let [start (js/Date. (:start_date iteration))
        end (js/Date. (:end_date iteration))
        now (.now js/Date)
        left (millis->days (- end now))
        passed (millis->days (- now start))]
    (-> iteration
        (assoc :days-left left)
        (assoc :days-passed passed))))

(def iteration-days
  (->interceptor
   :id :iteration-days
   :after (fn [context]
            (let [iterations (get-in context [:effects :db :iterations])]
              (assoc-in context [:effects :db :iterations]
                        (map add-date iterations))))))

(reg-event-fx
 :load-iterations
 (fn [{db :db} _]
   {:http-xhrio {:method :get
                 :uri "/api/iterations"
                 :format (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:iterations-loaded]
                 :on-failure [:bad-response]}}))

(reg-event-fx
 :iterations-loaded
 [iteration-days]
 (fn [{db :db} [_ response]]
   {:db (assoc db :iterations (js->clj response))
    :dispatch [:load-specification]}))


(reg-event-db
 :project-iterations
 (fn [db [_ id]]
   (->> (:iterations db)
        (filter #(= (:project_id %) id))
        (assoc db :project-iterations))))

(reg-event-db
 :show-iteration-specs
 (fn [db [_ id]]
   (->> (:specifications db)
        (filter #(= (:iteration_id %) id))
        (assoc-in db [:specs-by-iteration id]))))

(reg-event-fx
 :change-iteration-start
 (fn [{db :db} [_ id new-val]]
   {:db (update db :iterations
                #(update-coll-item-by-id % id :start_date new-val))
    :dispatch [:change-iteration-days-passed id]}))

(reg-event-fx
 :change-iteration-end
 (fn [{db :db} [_ id new-val]]
   {:db (update db :iterations
                #(update-coll-item-by-id % id :end_date new-val))
    :dispatch [:change-iteration-days-left id]}))

(reg-event-db
 :change-iteration-days-left
 (path :iterations)
 (fn [iterations [_ id]]
   (let [now (.now js/Date)
         end (js/Date. (:end_date (iter-by-id iterations id)))]
     (update-coll-item-by-id iterations id :days-left
                       (millis->days (- end now))))))


(reg-event-db
 :change-iteration-days-passed
 (path :iterations)
 (fn [iterations [_ id]]
   (let [now (.now js/Date)
         start (js/Date. (:start_date (iter-by-id iterations id)))]
     (update-coll-item-by-id iterations id :days-passed
                       (millis->days (- now start))))))


(reg-event-db
 :show-iteration-days-modal
 (fn [db [_ flag id]]
   (assoc-in db [:iteration-days-modal id] flag)))

(reg-event-db
 :change-iteration-desc
 (fn [db [_ id new-val]]
   (update db :iterations #(update-coll-item-by-id %
                                                   id
                                                   :description
                                                   new-val))))
(reg-event-db
 :toggle-edit-iteration-desc
 (fn [db [_ id]]
   (update-in db [:iteration-desc-editing id] #(not %))))

(reg-event-fx
 :validate-iteration-desc
 (fn [{db :db} [_ id name]]
   {:dispatch-n
    [(if-not (s/blank? name)
       (do
         [:toggle-edit-iteration-desc id]
         [:save-iteration id])
       [:notify :error "Iteration description should not be blank !"])]}))


(reg-event-fx
 :delete-iteration
 (fn [{db :db} [_ id]]
   {:http-xhrio {:method :post
                 :uri "/api/iteration/delete"
                 :params {:id id}
                 :format (ajax/json-request-format)
                 :response-format (ajax/text-response-format)
                 :on-success [:iteration-deleted id]
                 :on-failure [:bad-response]}
    :dispatch
    [:notify :operation "Deleting Iteration ..."]}))

(reg-event-fx
 :iteration-deleted
 (fn [{db :db} [_ id]]
   {:db (update db :iterations
                (fn [its] (remove #(= (:id %) id) its)))
    :dispatch
    [:notify :info (str "Iteration " (iter-num db id) " Deleted Successfully.")]}))

(reg-event-fx
 :create-new-iteration
 (fn [_ [_ id]]
   {:http-xhrio {:method :post
                 :uri "/api/iteration/create"
                 :params {:project-id id}
                 :format (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:iteration-created]
                 :on-failure [:bad-response]}
    :dispatch
    [:notify :operation "Create New Iteration ..."]}))


(reg-event-fx
 :save-iteration
 (fn [{db :db} [_ id]]
   {:http-xhrio {:method :post
                 :uri "/api/iteration/save"
                 :params {:iteration (-> (iter-by-id (:iterations db) id)
                                         (dissoc :days-left)
                                         (dissoc :days-passed))}
                 :format (ajax/json-request-format)
                 :response-format (ajax/text-response-format)
                 :on-success [:iteration-saved id]
                 :on-failure [:bad-response]}
    :dispatch [:notify :operation "Saving Iteration ..."]}))


(reg-event-fx
 :iteration-saved
 (fn [{db :db} [_ id]]
   {:dispatch
    [:notify :info (str "Iteration " (iter-num db id) " saved successfully.")]}))

(reg-event-fx
 :iteration-created
 (fn [{db :db} [_ response]]
   (let [iteration (js->clj response)]
     {:db (update db :iterations #(conj % iteration))
      :dispatch-n
      [[:change-iteration-days-left (:id iteration)]
       [:change-iteration-days-passed (:id iteration)]
       [:notify :info (str "Iteration (id : " (:id iteration) ") created successfully.")]]})))
