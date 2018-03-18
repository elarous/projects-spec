(ns projects-spec.views.iteration
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [projects-spec.views.modals :refer [iteration-days-modal
                                                project-days-modal]]
            [projects-spec.views.specification :refer [spec-view]]))


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

