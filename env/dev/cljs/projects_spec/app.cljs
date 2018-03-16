(ns ^:figwheel-no-load projects-spec.app
  (:require [projects-spec.core :as core]
            [devtools.core :as devtools]))

(enable-console-print!)

(devtools/install!)

(core/init!)
