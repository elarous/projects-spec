(ns projects-spec.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [projects-spec.core-test]))

(doo-tests 'projects-spec.core-test)

