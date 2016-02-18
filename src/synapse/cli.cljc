(ns synapse.cli
  (:refer-clojure :exclude [resolve])
  (:require #?(:clj  [clojure.tools.cli :refer [parse-opts]]
               :cljs [cljs.tools.cli :refer [parse-opts]])
            [clojure.string :as str]
            [synapse.io :as io]
            [synapse.core :refer [resolve-template resolve-file-template
                                  outfile-name]]
            [synapse.util :refer [pretty-print-errors]]))


(def ^:const VERSION "0.2.0")
(def ^:const FULL-VERSION (str "synapse-" VERSION))

(def help
"

                               synapse
                    ==============================
                        (C) Bruno Bonacci 2016
                               v%%VERSION%%

Description:
------------

  Synapse is the smart way to connect and configure docker containers.
  It uses a number of strategies to retrieve configuration and
  discover dependencies such as: environment variables, docker
  standard variables (to come: Kubernetes discovery API, Consul
  discovery API, Consul configuration API, etcd configuration API).

  At its simplest `synapse` takes environment variables and replaces
  them into configuration files, but there is more.  You can create
  your configuration files and put resolvable tags in the place where
  you expect values passed down to the container.  Synapse will then
  replace the tag (or placeholder) with the actual value.

Usage:
------
  synapse [options] file1.cfg.tmpl file2.yml.tmpl fileN.config.tmpl

Options:
--------
  -d   --debug     Prints debug information to the stderr
  -h   --help      This help screen
  -v   --version   Prints version information to the stderr

Tag Specifications:
-------------------

  Anatomy of a resolvable tag:

                 target pattern
       resolver       |           delimiter
           |          |           |
        %%docker>>zookeeper.*:2181%%
         |      |              |
  delimiter     |            port
             single or    (optional)
             multiple?


  %%HOME%% %%env>HOME%% %%DATA_DIR%%
  It will look for a matching environment variable,
  when found it will replace the tag with its value.

  %%env>>SERVICE.*%%
  A double angle bracket (`>>`) means that you expect more
  than one result. It will look for environment variables
  like: `$SERVICE1`, `$SERVICE2`, `$SERVICE3` (matching
  `SERVICE.*` regex) and replace the tag with the values
  as a comma-separated list.

  %%docker>zookeeper%%
  It will look for Docker's standard environment variables
  to link the container. For example it in this case it will
  look for: `$ZOOKEEPER_PORT_2181_TCP_ADDR`,
  `$ZOOKEEPER_PORT_2181_TCP_PORT` and `$ZOOKEEPER_PORT_2181_TCP`
  The docker resolution is the most commonly used so you can
  omit the `docker` resolver name.

  %%>zookeeper%%
  When the port is not specified it will look for all available
  port names and take the lowest. A service might have more
  than one service port (such as admin port or peers port)
  but typically the client port is the lowest one.

  %%>zookeeper:2181%%
  Otherwise you can specify the port you wish. This will be
  resolved and replaced with the actual container's port.

  %%>>zookeeper.*:2181%%
  If you expect more than one container with a given name
  then you can specify a pattern and add a double angle
  bracket (`>>`). In this case it will look for all zookeeper
  containers and replace the tag with a comma-separated list.

  $$DATA_DIR||/mnt/data$$ or $$env>DATA_DIR||/mnt/data$$ and
  $$>>zookeeper.*:2181||10.10.10.10:1221$$
  You can provide a default value which will be used in case
  no matching candidate env vars are found. Just add `||`
  (double pipe) followed by the default value. The default
  value can be also empty which will resolve to an empty
  string (eg: $$SOME_VAR||$$). This it can be useful
  in cases when you want to resolve a value if given
  but not fail it isn't set in the environment.

  %%[addr]>zookeeper.*:2181%% or %%[port]>zookeeper:2181%%
    => 172.17.0.2                   => 34765
  In some cases it might be useful to get just the host address or
  just the port number, for example when the configuration settings
  require two separate entries. In such case just add `[addr]` or
  `[port]` before the single or double angle bracket and the
  resolver will emit just the part you are interested in.
  Omitting this option is the same as `[addr,port]`

  %%[sep=;]>>zookeeper.*:2181%%
  You can customize the separator by adding `sep=` followed by the
  separator you wish to add. It can be empty, a single character or a
  string. `\\n` and `\\t` will be unescaped while the comma (`,`)
  is not supported as it is the default separator.


Usage examples:
---------------

  * simple template parsing (you can add more files)
  $ synapse /etc/haproxy/haproxy.conf.tmpl

  * if the system user can't write the output file
  $ cat /etc/haproxy/haproxy.conf.tmpl | \\
      synapse | sudo tee /etc/haproxy/haproxy.conf

For more information please refer to:

    https://github.com/BrunoBonacci/synapse

Copyright © 2016 Bruno Bonacci
Distributed under the Apache 2 License.
")


(def cli-options
  ;; An option with a required argument
  [["-d" "--debug"   "Print debug information"]
   ["-h" "--help"]
   ["-v" "--version" "Print version info."]
   ])


(defn usage []
  (let [template help]
    (-> (resolve-template {"VERSION" VERSION} template)
        :output
        (str/replace #"\$\$" "%%") )))


(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (str/join \newline errors)))



(defn process-file [file]
  (let [result (resolve-file-template
                (io/environment-map)
                file
                (outfile-name file))]
    (when (not= :ok (:resolution result))
      (io/show-message
       (pretty-print-errors (-> result :resolutions :fail) :file file)))
    (:resolution result)))



(defn main-process-files [files]
  (let [results (set (map process-file files))]
    (if (:with-errors results)
      (io/exit 1 nil)
      (io/exit 0 nil))))



(defn main-process-stdin []
  (let [result (resolve-template (io/environment-map) (io/read-stdin-all))]
    (if (= :with-errors (:resolution result))
      (io/exit 1 (pretty-print-errors (-> result :resolutions :fail)))
      (do (println (:output result)) (io/exit 0 nil)))))


(defn -main-cmd-line [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    ;; Handle help and error conditions
    (cond
      (:help options)          (io/exit 0 (usage))
      (:version options)       (io/exit 0 FULL-VERSION)
      errors (io/exit 1 (error-msg errors)))
    ;; Execute program with options
    (if (seq arguments)
      (main-process-files arguments)
      (main-process-stdin))))
