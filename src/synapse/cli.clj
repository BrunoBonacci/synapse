(ns synapse.cli
  (:refer-clojure :exclude [resolve])
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as str]
            [synapse.io :as io]
            [synapse.core :refer :all]
            [synapse.util :refer [pretty-print-errors]])
  (:gen-class))


(def cli-options
  ;; An option with a required argument
  [
   ;; verbose output
   ["-v" nil "Print debug information"
    :id :verbosity
    :default 0
    :assoc-fn (fn [m k _] (update-in m [k] inc))]

   ;; A boolean option defaulting to nil
   ["-h" "--help"]])


(defn usage []
  (let [template (str "\n" (io/read-resource-file "help.txt"))
        version  (str/trim (io/read-resource-file "synapse.version"))]
    (:output (resolve-template {"VERSION" version} template))))


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



(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    ;; Handle help and error conditions
    (cond
      (:help options)          (io/exit 0 (usage))
      errors (io/exit 1 (error-msg errors)))
    ;; Execute program with options
    (if (seq arguments)
      (main-process-files arguments)
      (main-process-stdin))))
