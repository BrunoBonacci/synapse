(defproject com.brunobonacci/synapse-core "0.4.0-SNAPSHOT"

  :description "Smart container linking system for Docker, Kubernetes et al."

  :url "https://github.com/BrunoBonacci/synapse"

  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.228"]
                 [com.lucasbradstreet/instaparse-cljs "1.4.1.0"] ;; clojurescript port
                 ]

  :profiles {:dev {:dependencies [[midje "1.6.3"]]
                   :plugins [[lein-midje "3.1.3"]]}}

  :deploy-repositories [["releases" :clojars]
                        ["snapshots" :clojars]]
  )
