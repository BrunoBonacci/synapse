(defproject com.brunobonacci/synapse "0.3.1"

  :description "Smart container linking system for Docker, Kubernetes et al."

  :url "https://github.com/BrunoBonacci/synapse"

  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [com.brunobonacci/synapse-core "0.3.0"]
                 ]

  :profiles {:dev {:dependencies [[midje "1.6.3"]]
                   :plugins [[lein-midje "3.1.3"]]}}

  :deploy-repositories [["releases" :clojars]
                        ["snapshots" :clojars]]
  )
