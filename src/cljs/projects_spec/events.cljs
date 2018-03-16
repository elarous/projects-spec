(ns projects-spec.events
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


;;TODO : add another notification type, the process type which stands for
;; any operation that may take some time, i think it will be nice to add a spinner 


(defn info [msg]
  (rf/dispatch [:notify :info msg]))

(defn error [msg]
  (rf/dispatch [:notify :error msg]))

(defn operation [msg]
  (rf/dispatch [:notify :operation msg]))

;; helper functions
;; TODO : move this to another place
(defn update-coll-item-by-id [coll id k new-val]
  (let [new-item (assoc (->> coll
                             (some #(and (= (:id %) id) %)))
                         k new-val)]
    (conj (remove #(= (:id %) id) coll)
                      new-item)))


(defn project-by-id [projects id]
  (some #(and (= (:id %) id) %) projects))

(defn project-name [db id]
  (:name (project-by-id (:projects db) id)))

(defn iter-by-id [iterations id]
  (some #(and (= (:id %) id) %) iterations))

(defn iter-num [db id]
  (:name (iter-by-id (:iterations db) id)))

(defn iters-by-project [iterations project-id]
  (filter #(and (= (:project_id %) project-id) %) iterations))


(defn millis->days [millis]
  (.ceil js/Math (quot millis 86400000)))

;; interceptors

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



;;dispatchers


(reg-event-db
  :initialize-db
  (fn [_ _]
    db/default-db))

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

(reg-event-db
 :bad-response
 (fn [db [_ response]]
   (js/alert response)
   db))

(reg-event-db
 :show-project
 (fn [db [_ id]]
   (let [p (some (fn [project]
                   (and (= (:id project) id) project))
                 (:projects db))]
     (assoc db :active-project p))))

(reg-event-db
  :set-active-page
  (fn [db [_ page]]
    (assoc db :page page)))

(reg-event-db
 :view-project
 [project-remaining-days]
 (fn [db [_ id]]
   (assoc db :viewing-project id)))

;; (reg-event-db
;;  :close-project-view
;;  (fn [db [_ id]]
;;    (dissoc db :viewing-project)))

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
 :change-project-start
 (fn [{db :db} [_ id new-val]]
   {:db (update db :projects
                #(update-coll-item-by-id % id :start_date new-val))
    :dispatch [:change-project-days-passed id]}))

(reg-event-fx
 :change-iteration-start
 (fn [{db :db} [_ id new-val]]
   {:db (update db :iterations
                #(update-coll-item-by-id % id :start_date new-val))
    :dispatch [:change-iteration-days-passed id]}))

(reg-event-fx
 :change-project-end
 (fn [{db :db} [_ id new-val]]
   {:db (update db :projects
                #(update-coll-item-by-id % id :end_date new-val))
    :dispatch [:change-project-days-left id]}))

(reg-event-fx
 :change-iteration-end
 (fn [{db :db} [_ id new-val]]
   {:db (update db :iterations
                #(update-coll-item-by-id % id :end_date new-val))
    :dispatch [:change-iteration-days-left id]}))

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
 :change-iteration-days-left
 (path :iterations)
 (fn [iterations [_ id]]
   (let [now (.now js/Date)
         end (js/Date. (:end_date (iter-by-id iterations id)))]
     (update-coll-item-by-id iterations id :days-left
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
   (if-not (s/blank? name)
     (do
       (rf/dispatch [:toggle-edit-iteration-desc id])
       (rf/dispatch [:save-iteration id]))
     (error "Iteration description should not be blank !"))))

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
 :delete-iteration
 (fn [{db :db} [_ id]]
   (operation "Deleting Iteration ...")
   {:http-xhrio {:method :post
                 :uri "/api/iteration/delete"
                 :params {:id id}
                 :format (ajax/json-request-format)
                 :response-format (ajax/text-response-format)
                 :on-success [:iteration-deleted id]
                 :on-failure [:bad-response]}}))

(reg-event-db
 :iteration-deleted
 (fn [db [_ id]]
   (info (str "Iteration " (iter-num db id) " Deleted Successfully."))
   (update db :iterations
           (fn [its] (remove #(= (:id %) id) its)))))

(reg-event-fx
 :create-new-iteration
 (fn [_ [_ id]]
   (operation "Create New Iteration ...")
   {:http-xhrio {:method :post
                 :uri "/api/iteration/create"
                 :params {:project-id id}
                 :format (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:iteration-created]
                 :on-failure [:bad-response]}}))

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

(reg-event-fx
 :save-iteration
 (fn [{db :db} [_ id]]
   (operation (str "Saving Iteration ..."))
   {:http-xhrio {:method :post
                 :uri "/api/iteration/save"
                 :params {:iteration (-> (iter-by-id (:iterations db) id)
                                         (dissoc :days-left)
                                         (dissoc :days-passed))}
                 :format (ajax/json-request-format)
                 :response-format (ajax/text-response-format)
                 :on-success [:iteration-saved id]
                 :on-failure [:bad-response]}}))

(reg-event-db
 :project-saved
 (fn [db [_ id]]
   (info (str "Project '" (project-name db id) "' saved successfully."))
    db))

(reg-event-db
 :iteration-saved
 (fn [db [_ id]]
   (info (str "Iteration " (iter-num db id) " saved successfully."))
   db))

;; a helper function to dispatch both events, i don't think this is how it should be done, however
(defn- change-iteration-both-days [id]
  (rf/dispatch [:change-iteration-days-left id])
  (rf/dispatch [:change-iteration-days-passed id]))

(reg-event-fx
 :iteration-created
 (fn [{db :db} [_ response]]
   (let [iteration (js->clj response)]
     (info (str "Iteration (id : " (:id iteration) ") created successfully."))
     (change-iteration-both-days (:id iteration))
     {:db (update db :iterations #(conj % iteration))})))

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

;;subscriptions

(reg-sub
  :page
  (fn [db _]
    (:page db)))

(reg-sub
  :docs
  (fn [db _]
    (:docs db)))

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
 :project-iterations
 (fn [db [_ project-id]]
   (sort-by :num (iters-by-project (:iterations db) project-id))))
(reg-sub
 :iteration-specs
 (fn [db [_ id]]
   (get-in db [:specs-by-iteration id])))

(reg-sub
 :completed-specs
 (fn [db [_]]
   (get-in db [:completed-specs])))

(reg-sub
 :iter-all-specs-count
 (fn [db [_ id]]
   (->> (:specifications db)
        (filter #(= (:iteration_id %) id))
        count)))

(reg-sub
 :iter-done-specs-count
 (fn [db [_ id]]
   (->> (:specifications db)
        (filter #(and (= (:iteration_id %) id)
                      (:implemented %)))
        count)))

(reg-sub
 :modal
 (fn [db [_]]
   (:modal db)))

(reg-sub
 :iteration-start-date
 (fn [db [_ id]]
   (:start_date (iter-by-id (:iterations db) id))))

(reg-sub
 :iteration-end-date
 (fn [db [_ id]]
   (:end_date (iter-by-id (:iterations db) id))))

(reg-sub
 :iteration-days-left
 (fn [db [_ id]]
   (:days-left (iter-by-id (:iterations db) id))))

(reg-sub
 :iteration-days-passed
 (fn [db [_ id]]
   (:days-passed (iter-by-id (:iterations db) id))))

(reg-sub
 :iteration-days-modal
 (fn [db [_ id]]
   (get-in db [:iteration-days-modal id])))

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
 :iteration-desc-editing
 (fn [db [_ id]]
   (get-in db [:iteration-desc-editing id])))

(reg-sub
 :iteration-desc
 (fn [db [_ id]]
   (:description (iter-by-id (:iterations db) id))))

(reg-sub
 :project-name
 (fn [db [_ id]]
   (:name (project-by-id (:projects db) id))))

(reg-sub
 :project-desc
 (fn [db [_ id]]
   (:description (project-by-id (:projects db) id))))


(reg-sub
 :iteration-num
 (fn [db [_ id]]
   (:num (iter-by-id (:iterations db) id))))

(reg-sub
 :project-img
 (fn [db [_ id]]
   (:img_src (project-by-id (:projects db) id))))

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
