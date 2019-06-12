(defproject com.brunobonacci/synapse-core "0.5.0"

  :description "Smart container linking system for Docker, Kubernetes et al."

  :url "https://github.com/BrunoBonacci/synapse"

  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 ;; clojurescript port
                 [com.lucasbradstreet/instaparse-cljs "1.4.1.2"]]

  :profiles {:dev {:dependencies [[midje "1.9.8"]]
                   :plugins [[lein-midje "3.2.1"]]}}

  )
