(ns synapse.cli
  (:refer-clojure :exclude [resolve])
  (:require #?(:clj  [clojure.tools.cli :refer [parse-opts]]
               :cljs [cljs.tools.cli :refer [parse-opts]])
            [clojure.string :as str]
            [synapse.io :as io]
            [synapse.core :refer [resolve-template resolve-file-template
                                  outfile-name]]
            [synapse.util :refer [pretty-print-errors]]))


(def cli-options
  ;; An option with a required argument
  [["-d" "--debug"   "Print debug information"]
   ["-h" "--help"]
   ["-v" "--version" "Print version info."]
   ])


(defn usage []
  (let [template (str "\n" (io/read-resource-file "help.txt"))
        version  (str/trim (io/read-resource-file "synapse.version"))]
    (-> (resolve-template {"VERSION" version} template)
        :output
        (str/replace #"\$\$" "%%") )))


(defn version []
  (let [version  (str/trim (io/read-resource-file "synapse.version"))]
    (str "synapse-" version)))


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
      (:version options)       (io/exit 0 (version))
      errors (io/exit 1 (error-msg errors)))
    ;; Execute program with options
    (if (seq arguments)
      (main-process-files arguments)
      (main-process-stdin))))