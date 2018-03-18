(ns projects-spec.views.common
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [markdown.core :refer [md->html]]
            [ajax.core :refer [GET POST]]
            [projects-spec.ajax :refer [load-interceptors!]]
            
            [projects-spec.views.modals :refer [iteration-days-modal
                                                project-days-modal]]))


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
