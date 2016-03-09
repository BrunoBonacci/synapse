(ns synapse.cli
  (:refer-clojure :exclude [resolve])
  (:require #?(:clj  [clojure.tools.cli :refer [parse-opts]]
               :cljs [cljs.tools.cli :refer [parse-opts]])
            [clojure.string :as str]
            [synapse.io :as io]
            [synapse.core :refer [resolve-template resolve-file-template
                                  outfile-name]]
            [synapse.util :refer [pretty-print-errors]]
            [synapse.help :refer [help]]))


(def ^:const VERSION "0.3.3")
(def ^:const FULL-VERSION (str "synapse-" VERSION))

(def cli-options
  ;; An option with a required argument
  [;["-d" "--debug"   "Print debug information"]
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
       (pretty-print-errors result :file file)))
    (:resolution result)))



(defn main-process-files [files]
  (let [results (set (map process-file files))]
    (if (= #{:ok} results)
      (io/exit 0 nil)
      (io/exit 1 nil))))



(defn-  process-line [line]
  (let [result (resolve-template (io/environment-map) line)]
    (when (not= :ok (:resolution result))
      (io/show-message
       (pretty-print-errors result)))
    (println (:output result))
    result))



(defn main-process-stdin []
  (io/on-stdin-by-line
   process-line
   (fn [results]
     (let [results-set  (set (map :resolution results))]
       (if (= #{:ok} results-set)
         (io/exit 0 nil)
         (io/exit 1 nil))))))


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
