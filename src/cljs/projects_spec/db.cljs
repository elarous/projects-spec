(ns projects-spec.db)

(def default-db
  {:page :home
   :home-panel [:h1 "Welcome"]
   :modal {:head ""
           :content ""
           :visible false}
   :notif {:visible false
           :notif-type :info
           :notif-content "Application Loaded"}
   :project-view-not-selected [:h1 "Please select a project"]})
