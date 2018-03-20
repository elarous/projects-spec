(ns ^:figwheel-load projects-spec.events.stakeholder
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
            [projects-spec.events.common :refer [update-coll-item-by-id]]))


(defn stakeholder-by-id [stakeholders id]
  (some #(and (= (:id %) id) %) stakeholders))

(defn stakeholder-name [db id]
  (:name (stakeholder-by-id (:stakeholders db) id)))

(reg-event-fx
 :load-stakeholders
 (fn [{db :db} _]
   {:http-xhrio {:method :get
                 :uri "/api/stakeholders"
                 :format (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:stakeholders-loaded]
                 :on-failure [:bad-response]}}))

(reg-event-db
 :stakeholders-loaded
 (fn [db [_ response]]
   (assoc db :stakeholders (js->clj response))))

(reg-event-db
 :change-stakeholder-fg
 (fn [db [_ id new-color]]
   (update db :stakeholders #(update-coll-item-by-id %
                                                     id
                                                     :fg_color
                                                     new-color))))

(reg-event-db
 :change-stakeholder-bg
 (fn [db [_ id new-color]]
   (update db :stakeholders #(update-coll-item-by-id %
                                                     id
                                                     :bg_color
                                                     new-color))))

(reg-event-db
 :change-stakeholder-name
 (fn [db [_ id new-name]]
   (update db :stakeholders #(update-coll-item-by-id %
                                                     id
                                                     :name
                                                     new-name))))

(reg-event-db
 :toggle-edit-stakeholder-name
 (fn [db [_ id]]
   (update-in db [:stakeholder-name-editing id] #(not %))))

(reg-event-db
 :change-stakeholder-desc
 (fn [db [_ id new-desc]]
   (update db :stakeholders #(update-coll-item-by-id %
                                                     id
                                                     :description
                                                     new-desc))))

(reg-event-db
 :toggle-edit-stakeholder-desc
 (fn [db [_ id]]
   (update-in db [:stakeholder-desc-editing id] #(not %))))

(reg-event-fx
 :validate-stakeholder-name
 (fn [{db :db} [_ id new-name]]
   {:dispatch-n
    [(if-not (s/blank? new-name)
       (do
         [:toggle-edit-stakeholder-name id]
         [:save-stakeholder id])
       [:notify :error "Stakeholder's name should not be empty !"])]}))

(reg-event-fx
 :validate-stakeholder-desc
 (fn [{db :db} [_ id new-desc]]
   {:dispatch-n
    [(if-not (s/blank? new-desc)
       (do
         [:toggle-edit-stakeholder-desc id]
         [:save-stakeholder id])
       [:notify :error "Stakeholder's description should not be empty !"])]}))

(reg-event-fx
 :save-stakeholder
 (fn [{db :db} [_ id]]
   {:http-xhrio {:method :post
                 :uri "/api/stakeholder/save"
                 :params {:stakeholder (stakeholder-by-id (:stakeholders db)
                                                          id)}
                 :format (ajax/json-request-format)
                 :response-format (ajax/text-response-format)
                 :on-success [:stakeholder-saved id]
                 :on-failure [:bad-response]}
    :dispatch
    [:notify :operation (str "Saving Stakeholder '" (stakeholder-name db id) "' ...")]}))

(reg-event-fx
 :stakeholder-saved
 (fn [{db :db} [_ id]]
   {:dispatch [:notify :info
               (str "Stakeholder '" (stakeholder-name db id) "' Saved.")]}))

(reg-event-fx
 :create-new-stakeholder
 (fn [{db :db} [_ project-id]]
   {:http-xhrio {:method :post
                 :uri "/api/stakeholder/create"
                 :params {:project_id project-id}
                 :format (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:stakeholder-created]
                 :on-failure [:bad-response]}
    :dispatch
    [:notify :operation "Creating New Stakeholder ... "]}))

(reg-event-fx
 :stakeholder-created
 (fn [{db :db} [_ response]]
   {:db (update db :stakeholders #(conj % (js->clj response)))
    :dispatch
    [:notify :info "New Stakeholder Created Successfully."]}))

(reg-event-fx
 :delete-stakeholder
 (fn [{db :db} [_ id]]
   {:http-xhrio {:method :post
                 :uri "/api/stakeholder/delete"
                 :params {:id id}
                 :format (ajax/json-request-format)
                 :response-format (ajax/text-response-format)
                 :on-success [:stakeholder-deleted id]
                 :on-failure [:bad-response]}
    :dispatch [:notify :operation (str "Deleting Stakeholder '"
                                       (stakeholder-name db id)
                                       "' ...")]}))

(reg-event-fx
 :stakeholder-deleted
 (fn [{db :db} [_ id]]
   {:db (update db :stakeholders
                #(remove (fn [stk] (= (:id stk) id)) %))
    :dispatch [:notify :info "Stakeholder Deleted."]}))
