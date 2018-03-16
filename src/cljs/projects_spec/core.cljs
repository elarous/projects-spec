(ns projects-spec.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [markdown.core :refer [md->html]]
            [ajax.core :refer [GET POST]]
            [projects-spec.ajax :refer [load-interceptors!]]
            [projects-spec.events]
            [projects-spec.modals :refer [iteration-days-modal
                                          project-days-modal]])
  (:import goog.History))


;; Notifier
(defn notifier []
  (let [notif-type @(rf/subscribe [:notif-type])
        class-name (if-not (nil? notif-type) (name notif-type) "")
        fa-icon (case notif-type
                  :info "fas fa-info-circle"
                  :error "fas fa-exclamation-circle"
                  "")]
    [:div#notifier-container
     {:class class-name
      :style {:bottom (if @(rf/subscribe [:notif-visible])
                        "0px"
                        "-28px")}
      :on-click #(rf/dispatch [:toggle-notif])}
     [:div#notifier
      [:i#notif-icon {:class fa-icon}]
      [:div#notif-content
       @(rf/subscribe [:notif-content])]
      (if (= notif-type :operation)
        [:div.loader])]]))

(defn spec-view [id]
  [:div.col-md-4.col-sm-6
   [:div.card.text-white.bg-dark.mb-3.text-left
    [:div.card-header
     [:i.fa.fa-user.mr-3]
     "A normal Spec"
     [:div.card-body
      [:div.card-title "In Order To do this and that"
       [:div.card-text "do this and that and that and that as well"]]]]]])


;; Iterations
(defn iteration-view-desc [id]
  (if @(rf/subscribe [:iteration-desc-editing id])
    [:input.iteration-view-desc-edit
     {:type "text"
      :placeholder "Iteration Description"
      :value @(rf/subscribe [:iteration-desc id])
      :on-focus #(-> % .-target .select)
      :on-change #(rf/dispatch [:change-iteration-desc
                                id
                                (-> % .-target .-value)])
      :on-blur #(rf/dispatch [:validate-iteration-desc
                              id
                              (-> % .-target .-value)])
      :auto-focus true}]
    [:h1.iteration-view-desc.clickable
     {:on-click #(rf/dispatch [:toggle-edit-iteration-desc id])}
     @(rf/subscribe [:iteration-desc id])]))

(defn iteration-view-head [id]
  [:div.row
   [iteration-days-modal id] 
   [:div.col-md-12
    [:div.row.p-2
     [:div.col-md-2.col-sm-6.iteration-days-left.order-sm-1.order-md-1
      [:div.row.iteration-days-num.clickable
       {:on-click #(rf/dispatch [:show-iteration-days-modal true id])}
       [:div.col-sm-12
        @(rf/subscribe [:iteration-days-left id])]]
      [:div.row.iteration-days-text
       [:div.col-sm-12
        "Days Left."]]]
     [:div.col-md-8.order-sm-3.order-md-2.my-4.py-4
      [iteration-view-desc id]]
     [:div.col-md-2.col-sm-6.iteration-days-left.order-sm-2.order-md-3
      [:div.row.iteration-days-num.clickable
       {:on-click #(rf/dispatch [:show-iteration-days-modal true id])}
       [:div.col-sm-12
        @(rf/subscribe [:iteration-days-passed id])]]
      [:div.row.iteration-days-text
       [:div.col-sm-12
        "Days Passed."]]]]]])

(defn iteration-view-specs-area [id]
  [:div.row.col-md-12.d-flex.justify-content-around
   (for [spec @(rf/subscribe [:iteration-specs id])]
     ^{:key (:id spec)} [spec-view (:id spec)])])

(defn iteration-view [id]
  [:div.row.iteration
   {:on-click #(rf/dispatch [:show-iteration-specs id])}
   [:div.col-md-1.badge.badge-pill.iteration-num.m-2
    @(rf/subscribe [:iteration-num id])]
   [:div.col-sm-12
    [iteration-view-head id]
    [iteration-view-specs-area id]
    [:div.row
     [:div.offset-sm-3.col-sm-6.offset-md-5.col-md-2
      [:button.btn.btn-danger.btn-block
       {:on-click #(rf/dispatch [:delete-iteration id])}
       "Delete Iteration"]]]]])

;; Project
(defn project-view-status [id]
  [:div
   [:div.row
    [:div.col-sm-12
     [project-days-modal id]]]
   [:div.row.project-view-status.clickable
    {:on-click #(rf/dispatch [:show-project-days-modal true id])}
    [:div.col-md-6
     [:div.row.my-2
      [:div.col-sm-6.project-view-status-label  "Status"]
      [:div.col-sm-6.project-view-status-value
       [:span.badge.badge-danger "Unfinished"]]]
     [:div.row.my-2
      [:div.col-sm-6.project-view-status-label "Finished Specs"]
      [:div.col-sm-6.project-view-status-value
       [:span.badge.badge-success @(rf/subscribe [:completed-specs])]]]]
    [:div.col-md-6
     [:div.row.my-2
      [:div.col-sm-6.project-view-status-label "Remaining Days"]
      [:div.col-sm-6.project-view-status-value
       [:span.badge.badge-info
        @(rf/subscribe [:project-days-left id])]]]
     [:div.row.my-2
      [:div.col-sm-6.project-view-status-label "Passed Days"]
      [:div.col-sm-6.project-view-status-value
       [:span.badge.badge-warning.text-white
        @(rf/subscribe [:project-days-passed id])]]]]]])

(defn project-view-desc [id]
  (if @(rf/subscribe [:project-desc-editing])
    [:input.project-view-desc-edit
     {:type "text"
      :placeholder "Project Description"
      :value @(rf/subscribe [:project-desc id])
      :on-focus #(-> % .-target .select)
      :on-change #(rf/dispatch [:change-project-desc
                                id
                                (-> % .-target .-value)])
      :on-blur #(rf/dispatch [:validate-project-desc id (-> % .-target .-value)])
      :auto-focus true}]
    [:h1.project-view-desc.clickable
     {:on-click #(rf/dispatch [:toggle-edit-project-desc id])}
     @(rf/subscribe [:project-desc id])]))


(defn project-view-name [id]
  (if @(rf/subscribe [:project-name-editing])
    [:input.display-3.project-view-name-edit
     {:type "text"
      :placeholder "Project Title"
      :value @(rf/subscribe [:project-name id])
      :on-focus #(-> % .-target .select)
      :on-change #(rf/dispatch [:change-project-name
                                id
                                (-> % .-target .-value)])
      :on-blur #(rf/dispatch [:validate-project-name id (-> % .-target .-value)])
      :auto-focus true}]
    [:h1.display-3.project-view-name.clickable
     {:on-click #(rf/dispatch [:toggle-edit-project-name id])}
     @(rf/subscribe [:project-name id])]))

(defn add-iteration-btn [project-id]
  [:i#add-iteration-btn.fas.fa-plus-circle.clickable
   {:on-click #(rf/dispatch [:create-new-iteration project-id])}])

(defn project-view-page []
  (if-let [id @(rf/subscribe [:project-view])]
    [:div.container.pt-3
     [:div.row
      [:div.col-sm-12
       [project-view-name id]
       [:i.project-view-quote-icon {:class "fa fa-quote-left"}]
       [project-view-desc id]
       [project-view-status id]
       (if-let [iterations @(rf/subscribe [:project-iterations id])]
         (for [iteration iterations]
           ^{:key (:id iteration)} [iteration-view (:id iteration)]))
       [:a.btn.btn-primary {:href "#/"
                            ;;:on-click ;;#(rf/dispatch [:close-project-view])
                            }
        [:i.fas.fa-arrow-circle-left " Back Home"]]]]
     [:div.row
      [:div.col-sm-12
       [add-iteration-btn id]]]
     [notifier]]
    [:h1.alert.alert-danger.text-center "No Project Selected !"]))



;; Home page

(defn add-project-btn []
  [:i#add-project-btn.fas.fa-plus-circle.clickable
   {:on-click #(rf/dispatch [:create-new-projet])}])

(defn project-card-img [id]
  [:div
   [:input
    {:type "file" :style {:display "none"}
     :id (str "project-img-chooser_" id)
     :on-change
     #(rf/dispatch [:change-project-img id (-> % .-target .-files)])}]
   [:img.card-img-bottom.clickable
    {:src (str js/context "/img/"
               @(rf/subscribe [:project-img id]))
     :alt "project image"
     :on-click #(->> (str "project-img-chooser_" id)
                    (.getElementById js/document )
                   .click)}]])

(defn project-card [id]
  [:div.card.text-center.project-card.mb-1
   [:button.project-delete-btn.clickable
    {:on-click #(rf/dispatch [:delete-project id])}
    "X"]
   [:div.card-body
    [project-card-img id]
    [:h3.card-title.mt-2 @(rf/subscribe [:project-name id])]
    [:p.card-text @(rf/subscribe [:project-desc id])]
    [:a.btn.btn-primary.d-block {:href "#/project-view"
                         :on-click (fn [] (rf/dispatch [:view-project id])
                                     (rf/dispatch [:project-iterations id]))}
     "View Project"]]])

(defn projects-group [projects]
  [:div.row
   (for [p projects]
     ^{:key (:id p)}
     [:div.col-sm-6
      [project-card (:id p)]])])

(defn projects-panel []
  [:div.row
   [:div.col-md-12
    [projects-group @(rf/subscribe [:projects])]
    [add-project-btn]]])

;; Navigation

(defn nav-link [uri title page]
  [:li.nav-item
   {:class (when (= page @(rf/subscribe [:page])) "active")}
   [:a.nav-link {:href uri} title]])

(defn navbar []
  [:nav.navbar.navbar-dark.bg-primary.navbar-expand-md
   {:role "navigation"}
   [:button.navbar-toggler.hidden-sm-up
    {:type "button"
     :data-toggle "collapse"
     :data-target "#collapsing-navbar"}
    [:span.navbar-toggler-icon]]
   [:a.navbar-brand {:href "#/"} "projects-spec"]
   [:div#collapsing-navbar.collapse.navbar-collapse
    [:ul.nav.navbar-nav.mr-auto
     [nav-link "#/" "Home" :home]
     [nav-link "#/about" "About" :about]]]])

(defn about-page []
  [:div.container
   [:div.row
    [:div.col-md-12
     [:h1 "Heyyy"]
     [:img {:src (str js/context "/img/warning_clojure.png")}]]]])



(defn home-page []
  [:div.container.pt-3
   [:div.row.d-flex.justify-content-around
    [projects-panel]]
   [notifier]])


(def pages
  {:home #'home-page
   :about #'about-page
   :project-view #'project-view-page
   })

(defn page []
  [:div
   [navbar]
   [(pages @(rf/subscribe [:page]))]
   ])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (rf/dispatch [:set-active-page :home]))

(secretary/defroute "/about" []
  (rf/dispatch [:set-active-page :about]))

(secretary/defroute "/project-view" []
  (rf/dispatch [:set-active-page :project-view]))


;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      HistoryEventType/NAVIGATE
      (fn [event]
        (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app

(defn mount-components []
  (rf/clear-subscription-cache!)
  (r/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (rf/dispatch-sync [:initialize-db])
  (rf/dispatch [:load-projects])
  (load-interceptors!)
  (hook-browser-navigation!)
  (mount-components)
  (rf/dispatch [:notify :info "Application Loaded Successfully !"]))

