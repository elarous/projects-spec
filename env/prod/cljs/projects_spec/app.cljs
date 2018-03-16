(ns projects-spec.app
  (:require [projects-spec.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
