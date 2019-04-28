(defproject com.brunobonacci/synapse "0.4.0"
  ; when updating the version please update cli.clj as well.
  :description "Smart container linking system for Docker, Kubernetes et al."

  :url "https://github.com/BrunoBonacci/synapse"

  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :sub
  ["synapse-core"
   "synapse-cli"
   "synapse-lib"]

  :plugins [[lein-sub "0.3.0"]]

  )
