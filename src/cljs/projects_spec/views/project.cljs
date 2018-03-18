(ns projects-spec.views.project
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [projects-spec.views.modals :refer [iteration-days-modal
                                                project-days-modal]]
            [projects-spec.views.common :refer [notifier]]
            [projects-spec.views.iteration :refer [iteration-view]]))

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



