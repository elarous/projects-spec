(ns projects-spec.events.project
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
            [projects-spec.events.common :refer [info
                                                 error
                                                 operation
                                                 update-coll-item-by-id
                                                 millis->days]]))



(defn project-by-id [projects id]
  (some #(and (= (:id %) id) %) projects))

(defn project-name [db id]
  (:name (project-by-id (:projects db) id)))


(def project-remaining-days
  (->interceptor
   :id :project-remaining-days
   :after (fn [context]
            (let [db (get-in context [:effects :db])
                  p (->> (get db :viewing-project context)
                         (project-by-id (:projects db)))
                  start (js/Date. (:start_date p))
                  end (js/Date. (:end_date p))
                  now (.now js/Date)
                  left (millis->days (- end now))
                  passed (millis->days (- now start))]
              (-> context
                  (update-in [:effects :db :projects]
                             #(update-coll-item-by-id %
                                                      (:id p)
                                                      :days-left
                                                      left))
                  (update-in [:effects :db :projects]
                             #(update-coll-item-by-id %
                                                      (:id p)
                                                      :days-passed
                                                      passed)))))))



(reg-event-fx
 :load-projects
 (fn [{db :db} _]
   {:http-xhrio {:method :get
                 :uri "/api/projects"
                 :format (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:projects-loaded]
                 :on-failure [:bad-response]}}))

(reg-event-fx
 :projects-loaded
 (fn [{db :db} [_ response]]
   {:db (assoc db :projects (js->clj response))
    :dispatch [:load-iterations]}))


(reg-event-db
 :show-project
 (fn [db [_ id]]
   (let [p (some (fn [project]
                   (and (= (:id project) id) project))
                 (:projects db))]
     (assoc db :active-project p))))

(reg-event-db
 :view-project
 [project-remaining-days]
 (fn [db [_ id]]
   (assoc db :viewing-project id)))


(reg-event-fx
 :change-project-start
 (fn [{db :db} [_ id new-val]]
   {:db (update db :projects
                #(update-coll-item-by-id % id :start_date new-val))
    :dispatch [:change-project-days-passed id]}))

(reg-event-fx
 :change-project-end
 (fn [{db :db} [_ id new-val]]
   {:db (update db :projects
                #(update-coll-item-by-id % id :end_date new-val))
    :dispatch [:change-project-days-left id]}))


(reg-event-db
 :change-project-days-left
 [(path :projects)]
 (fn [projects [_ id]]
   (let [now (.now js/Date)
         end (js/Date. (:end_date (project-by-id projects id)))]
     (update-coll-item-by-id projects
                             id
                             :days-left
                             (millis->days (- end now))))))


(reg-event-db
 :change-project-days-passed
 [(path :projects)]
 (fn [projects [_ id]]
   (let [now (.now js/Date)
         start (js/Date. (:start_date (project-by-id projects id)))]
     (update-coll-item-by-id projects
                             id
                             :days-passed
                             (millis->days (- now start))))))



(reg-event-db
 :show-project-days-modal
 (fn [db [_ flag id]]
   (assoc-in db [:project-days-modal id] flag)))


(reg-event-db
 :change-project-name
 (fn [db [_ id new-val]]
   (update db :projects #(update-coll-item-by-id %
                                                 id
                                                 :name
                                                 new-val))))



(reg-event-db
 :toggle-edit-project-name
 (fn [db [_]]
   (update db :project-name-editing #(not %))))


(reg-event-fx
 :validate-project-name
 (fn [{db :db} [_ id name]]
   (if-not (s/blank? name)
     (do
       (rf/dispatch [:toggle-edit-project-name])
       (rf/dispatch [:save-project id]))
     (error "Project name should not be blank !"))))


(reg-event-db
 :change-project-desc
 (fn [db [_ id new-val]]
   (update db :projects #(update-coll-item-by-id %
                                                 id
                                                 :description
                                                 new-val))))

(reg-event-db
 :toggle-edit-project-desc
 (fn [db [_]]
   (update db :project-desc-editing #(not %))))

(reg-event-fx
 :validate-project-desc
 (fn [{db :db} [_ id desc]]
   (if-not (s/blank? desc)
     (do
       (rf/dispatch [:toggle-edit-project-desc])
       (rf/dispatch [:save-project id]))
     (error "Project desc should not be blank !"))))


(reg-event-fx
 :create-new-projet
 (fn [_ [_]]
   (operation "Creating New Project ...")
   {:http-xhrio {:method :post
                 :uri "/api/project/create"
                 :format (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:project-created]
                 :on-failure [:bad-response]}}))

(reg-event-db
 :project-created
 (fn [db [_ response]]
   (info "Project Created Successfully.")
   (update db :projects #(conj % (js->clj response)))))


(reg-event-fx
 :change-project-img
 (fn [{db :db} [_ id files]]
   (operation "Uploading Project Image ...")
   (let [file (aget files 0)
         ext (-> (.-name file)
                 (s/split #"\.")
                 last)
         filename (str id "_" (.now js/Date) "." ext)
         form-data (doto (js/FormData.)
                     (.append "project-id" id)
                     (.append "file" file filename))]
     {:http-xhrio {:method :post
                   :uri "/api/project/update-img"
                   :body form-data
                   :response-format (ajax/raw-response-format)
                   :timeout 5000
                   :on-success [:project-img-changed id filename]
                   :on-failure [:bad-response]}})))


(reg-event-db
 :project-img-changed
 (fn [db [_ id filename]]
   (info "Project Image Changed Successfully.")
   (update db :projects #(update-coll-item-by-id %
                                                 id
                                                 :img_src
                                                 filename))))


(reg-event-fx
 :delete-project
 (fn [{db :db} [_ id]]
   (operation "Deleting Project ...")
   {:http-xhrio {:method :post
                 :uri "/api/project/delete"
                 :params {:id id}
                 :format (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:project-deleted id]
                 :on-failure [:bad-response]}}))


(reg-event-db
 :project-deleted
 (fn [db [_ id]]
   (info (str "Project '" (project-name db id) "' Deleted Successfully."))
   (update db :projects
           (fn [prs] (remove #(= (:id %) id) prs)))))

(reg-event-fx
 :save-project
 (fn [{db :db} [_ id]]
   (operation (str "Saving Project '" (project-name db id) "' ..."))
   {:http-xhrio {:method :post
                 :uri "/api/project/save"
                 :params {:project (-> (project-by-id (:projects db) id)
                                       (dissoc :days-left)
                                       (dissoc :days-passed))}
                 :format (ajax/json-request-format)
                 :response-format (ajax/text-response-format)
                 :on-success [:project-saved id]
                 :on-failure [:bad-response]}}))

(reg-event-db
 :project-saved
 (fn [db [_ id]]
   (info (str "Project '" (project-name db id) "' saved successfully."))
   db))
