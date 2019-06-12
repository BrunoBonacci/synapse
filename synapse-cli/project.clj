(defproject com.brunobonacci/synapse-cli "0.5.0"
  ;; when updating the version please update cli.clj as well.
  :description "Smart container linking system for Docker, Kubernetes et al."

  :url "https://github.com/BrunoBonacci/synapse"

  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [com.brunobonacci/synapse-core "0.5.0"]
                 [org.clojure/tools.cli "0.4.2"]]

  :main synapse.main

  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[midje "1.9.8"]]
                   :plugins [[lein-midje "3.2.1"]]
                   :repl-options
                   {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}}

  :plugins [[lein-shell "0.5.0"]
            [lein-binplus "0.6.5"]]

  :uberjar-name "synapse-standalone.jar"


  :aliases
  {;; Assumes local machine is a Mac
   "native-mac"
   ["shell"
    "native-image" "--report-unsupported-elements-at-runtime"
    "--initialize-at-build-time"
    "-jar" "target/synapse-standalone.jar"
    "-H:Name=target/synapse-Darwin-x86_64" ]

   ;; assumes container on Mac with /tmp shared with DockerVM
   "native-linux"
   ["do"
    "shell" "mkdir" "-p" "/tmp/target/,"
    "shell" "cp" "./target/synapse-standalone.jar" "/tmp/target/,"
    "shell"
    "docker" "run" "-v" "/tmp:/synapse" "findepi/graalvm:all"
    "/graalvm/bin/native-image" "--report-unsupported-elements-at-runtime"
    "-jar" "/synapse/target/synapse-standalone.jar"
    "-H:Name=/synapse/target/synapse-Linux-x86_64,"
    "shell" "cp" "/tmp/target/synapse-Linux-x86_64" "./target/"

    ;; docker run -ti -v /tmp:/synapse findepi/graalvm:all /bin/bash
    ;; /graalvm/bin/native-image  --report-unsupported-elements-at-runtime -jar /synapse/target/synapse-standalone.jar -H:Name=/synapse/target/synapse-Linux-x86_64
    ;;
    ]

   "native"
   ["do" "clean," "bin," "native-mac," "native-linux"]

   ;; prep release upload
   "package-native"
   ["do" "shell" "./bin/package-native.sh"]
   }


  :bin {:name "synapse"
        :jvm-opts ["-server" "-Dfile.encoding=utf-8" "$JVM_OPTS" ]}
  )
