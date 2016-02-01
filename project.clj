(defproject com.brunobonacci/synapse "0.1.0-SNAPSHOT"

  :description "Smart container linking system for Docker, Kubernetes et al."

  :url "https://github.com/BrunoBonacci/synapse"

  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [instaparse "1.4.1"]]

  :profiles {:dev {:dependencies [[midje "1.6.3"]]
                   :plugins [[lein-midje "3.1.3"]
                             [lein-bin "0.3.5"]]}}

)
