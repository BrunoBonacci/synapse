(defproject com.brunobonacci/synapse-cli "0.3.3"
  ;; when updating the version please update cli.clj as well.
  :description "Smart container linking system for Docker, Kubernetes et al."

  :url "https://github.com/BrunoBonacci/synapse"

  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.228"]
                 [com.brunobonacci/synapse-core "0.3.3"]
                 [org.clojure/tools.cli "0.3.3"]]

  :main synapse.main

  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[com.cemerick/piggieback "0.2.1"]
                                  [org.clojure/tools.nrepl "0.2.10"]
                                  [midje "1.6.3"]]
                   :plugins [[lein-midje "3.1.3"]]
                   :repl-options
                   {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}}

  :plugins [[lein-cljsbuild "1.1.2"]
            [lein-shell "0.5.0"]
            [lein-bin "0.3.5"]]


  :aliases {"docker"
            ["shell" "docker" "build" "-t" "brunobonacci/synapse:${:version}" "."]

            "docker-latest"
            ["shell" "docker" "build" "-t" "brunobonacci/synapse" "."]

            "node"
            ["do" "clean,"
             "cljsbuild" "once"]

            "exe"
            ["do" "clean,"
             "cljsbuild" "once,"
             "shell" "nexe" "-f"
             "-i" "./target/synapse.js"
             "-o" "./target/exe/synapse-${:version}"
             "-t" "./target/tmp/nexe"]
            }


  :cljsbuild
  {:builds
   [{:compiler
     {:target :nodejs,
      :output-to "target/synapse.js",
      :verbose true,
      :optimizations :simple,
      :pretty-print true},
     :source-paths ["src"]}]}

  :bin {:name "synapse" :bootclasspath false}
  )
