(ns projects-spec.views.specification
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [markdown.core :refer [md->html]]
            [ajax.core :refer [GET POST]]
            [projects-spec.ajax :refer [load-interceptors!]]
            ))

(defn spec-view [id]
  [:div.col-md-4.col-sm-6
   [:div.card.text-white.bg-dark.mb-3.text-left
    [:div.card-header
     [:i.fa.fa-user.mr-3]
     "A normal Spec"
     [:div.card-body
      [:div.card-title "In Order To do this and that"
       [:div.card-text "do this and that and that and that as well"]]]]]])
