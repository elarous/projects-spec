(ns projects-spec.views.modals
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            ))

(defn head [{:keys [text on-close-click]}]
  [:div.row.modal-head
   [:div.col-sm-10 text]
   [:div.col-sm-2
    [:button.close {:on-click on-close-click}
     "X"]]])

(defn start [{:keys [value max on-change]}]
  [:div.row.m-2
   [:div.col-sm-6
    [:h4 "Start Date"]]
   [:div.col-sm-6
    [:input.form-control
     {:type "date"
      :max max
      :value value
      :on-change on-change}]]])

(defn end [{:keys [value min on-change]}]
  [:div.row.m-2
   [:div.col-sm-6
    [:h4 "End Date"]]
   [:div.col-sm-6
    [:input.form-control
     {:type "date"
      :min min
      :value value
      :on-change on-change}]]])


(defn days-left [dl]
  (if (>= dl 0)
    [:div.row.m-2
     [:div.col-sm-6
      [:h4 "Days Left"]]
     [:div.col-sm-6
      [:h4.badge.badge-success
       dl]]]
    [:div.row.m-2
     [:div.col-sm-12
      [:h4.badge.badge-info "Already Finished!"]]]))

(defn days-passed [dp]
  (if (>= dp 0)
    [:div.row.m-2
     [:div.col-sm-6
      [:h4 "Days Passed"]]
     [:div.col-sm-6
      [:h4.badge.badge-danger
       dp]]]
    [:div.row.m-2
     [:div.col-sm-12
      [:h4.badge.badge-info "Not Started Yet !"]]]))

(defn iteration-days-modal [id]
  (if @(rf/subscribe [:iteration-days-modal id])
    [:div.modal-overlay
     [:div.modal-panel
      [head {:text "Edit Iteration Starting & Ending Dates"
             :on-close-click
             (fn []
               (rf/dispatch [:show-iteration-days-modal false id])
               (rf/dispatch [:save-iteration id]))}]
      [:div.modal-content
       [:div.row
        [:div.col-md-6.col-sm-12
         [start
          {:value @(rf/subscribe [:iteration-start-date id])
           :max @(rf/subscribe [:iteration-end-date id])
           :on-change #(rf/dispatch [:change-iteration-start
                                     id
                                     (-> % .-target .-value)])}]]
        [:div.col-md-6.col-sm-12
         [end
          {:value @(rf/subscribe [:iteration-end-date id])
           :min @(rf/subscribe [:iteration-start-date id])
           :on-change #(rf/dispatch [:change-iteration-end
                                     id
                                     (-> % .-target .-value)])}]]]
       [:div.row
        [:div.col-md-6.col-sm-12
         [days-passed
          @(rf/subscribe [:iteration-days-passed id])]]
        [:div.col-md-6.col-sm-12
         [days-left
          @(rf/subscribe [:iteration-days-left id])]]]]]]))


;; project
(defn project-days-modal [id]
  (if @(rf/subscribe [:project-days-modal id])
    [:div.modal-overlay
        [:div.modal-panel
         [head {:text "Edit Project Starting & Ending Dates"
                :on-close-click
                (fn []
                  (rf/dispatch [:show-project-days-modal false id])
                  (rf/dispatch [:save-project id]))}]
          [:div.modal-content
          [:div.row
            [:div.col-md-6.col-sm-12
             [start
              {:value @(rf/subscribe [:project-start-date id])
               :max @(rf/subscribe [:project-end-date id])
               :on-change #(rf/dispatch [:change-project-start
                                         id
                                         (-> % .-target .-value)])}]]
            [:div.col-md-6.col-sm-12
             [end
              {:value @(rf/subscribe [:project-end-date id])
               :min @(rf/subscribe [:project-start-date id])
               :on-change #(rf/dispatch [:change-project-end
                                         id
                                         (-> % .-target .-value)])}]]]
          [:div.row
            [:div.col-md-6.col-sm-12
             [days-passed
              @(rf/subscribe [:project-days-passed id])]]
            [:div.col-md-6.col-sm-12
             [days-left
              @(rf/subscribe [:project-days-left id])]]]]]]))
