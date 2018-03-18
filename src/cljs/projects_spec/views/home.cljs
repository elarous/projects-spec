(ns projects-spec.views.home
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [projects-spec.ajax :refer [load-interceptors!]]
            
            [projects-spec.views.modals :refer [iteration-days-modal
                                                project-days-modal]]
            [projects-spec.views.common :refer [notifier]]))


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



(defn home-page []
  [:div.container.pt-3
   [:div.row.d-flex.justify-content-around
    [projects-panel]]
   [notifier]])
