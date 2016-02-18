(defproject com.brunobonacci/synapse "0.2.0"

  :description "Smart container linking system for Docker, Kubernetes et al."

  :url "https://github.com/BrunoBonacci/synapse"

  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.228"]
                 [com.lucasbradstreet/instaparse-cljs "1.4.1.0"]
                 [org.clojure/tools.cli "0.3.3"]]

  :main synapse.main

  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[midje "1.6.3"]]
                   :plugins [[lein-midje "3.1.3"]]}}

  :plugins [[lein-cljsbuild "1.1.2"]]

  :cljsbuild
  {:builds
   [{:compiler
     {:target :nodejs,
      :output-to "target/synapse.js",
      :verbose true,
      :optimizations :simple,
      :pretty-print true},
     :source-paths ["src"]}]}
  )
