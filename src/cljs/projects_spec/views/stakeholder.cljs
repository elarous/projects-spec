(ns ^:figwheel-load projects-spec.views.stakeholder
  (:require [re-frame.core :as rf]))

(defn stakeholder-name [id]
  (if-not @(rf/subscribe [:stakeholder-name-editing id])
    [:div.card-title.stakeholder-name
     {:on-click #(rf/dispatch [:toggle-edit-stakeholder-name id])}
     @(rf/subscribe [:stakeholder-name id])]
    [:input.stakeholder-name-edit
     {:type "text"
      :value @(rf/subscribe [:stakeholder-name id])
      :on-change #(rf/dispatch [:change-stakeholder-name
                                id
                                (-> % .-target .-value)])
      :on-blur #(rf/dispatch [:validate-stakeholder-name
                              id
                              (-> % .-target .-value)])
      :auto-focus true
      :on-focus #(-> % .-target .select)
      :placeholder "Stakeholder Name"
      :style {:background-color @(rf/subscribe [:stakeholder-bg id])
              :color @(rf/subscribe [:stakeholder-fg id])}
      }]))

(defn stakeholder-desc [id]
  (if-not @(rf/subscribe [:stakeholder-desc-editing id])
    [:div.card-text.stakeholder-desc
     {:on-click #(rf/dispatch [:toggle-edit-stakeholder-desc id])}
     @(rf/subscribe [:stakeholder-desc id])]
    [:textarea.stakeholder-desc-edit
     {:value @(rf/subscribe [:stakeholder-desc id])
      :on-change #(rf/dispatch [:change-stakeholder-desc
                                id
                                (-> % .-target .-value)])
      :on-blur #(rf/dispatch [:validate-stakeholder-desc
                              id
                              (-> % .-target .-value)])
      :auto-focus true
      :on-focus #(-> % .-target .select)
      :placeholder "Stakeholder Short Description"
      :style {:background-color @(rf/subscribe [:stakeholder-bg id])
              :color @(rf/subscribe [:stakeholder-fg id])}}]))


(defn stakeholder-fg [id]
  [:input {:type "color"
           :on-change #(rf/dispatch [:change-stakeholder-fg
                                     id
                                     (-> % .-target .-value)])
           :value @(rf/subscribe [:stakeholder-fg id])
           :on-blur #(rf/dispatch [:save-stakeholder id])}])

(defn stakeholder-bg [id]
  [:input {:type "color"
           :on-change #(rf/dispatch [:change-stakeholder-bg
                                     id
                                     (-> % .-target .-value)])
           :value @(rf/subscribe [:stakeholder-bg id])
           :on-blur #(rf/dispatch [:save-stakeholder id])}])

(defn delete-stakeholder-btn [id]
  [:div.col-sm-6
   [:button.btn.btn-danger.m-0.p-1.stakeholder-delete-btn
    {:on-click #(rf/dispatch [:delete-stakeholder id])}
    [:i.fa.fa-trash]]])

(defn stakeholder-view [id]
  [:div.card.text-center.stakeholder-card
   [:div.card-header
    [:div.row
     [:div.col-sm-6
      [:span.stakeholder-specs-num
       {:style {:background-color @(rf/subscribe [:stakeholder-bg id])
                :color @(rf/subscribe [:stakeholder-fg id])}}
       @(rf/subscribe [:stakeholder-specs-count id])]
      [:span " Specs"]]
     [delete-stakeholder-btn id]]]
   [:div.card-body
    {:style {:background-color @(rf/subscribe [:stakeholder-bg id])
            :color @(rf/subscribe [:stakeholder-fg id])}}
    [stakeholder-name id]
    [stakeholder-desc id]]
   [:div.card-footer
    [:div.row.mb-2
     [:div.col-sm-6.stakeholder-fg-control
      [:i.mr-2.fas.fa-font]
      [stakeholder-fg id]]
     [:div.col-sm-6.stakeholder-bg-control
      [:i.mr-2.fas.fa-sticky-note]
      [stakeholder-bg id]]]]])

(defn add-stakeholder-btn [project-id]
  [:div.col-sm-6.col-md-4.d-flex.justify-content-center.align-items-center.flex-column
   [:div.fa.fa-plus.add-stakeholder
    {:on-click #(rf/dispatch [:create-new-stakeholder project-id])}]
   [:div.add-stakeholder-msg "Add Stakeholder"]])


