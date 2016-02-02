(ns synapse.cli
  (:require [clojure.tools.cli :refer [parse-opts]]))


(def cli-options
  ;; An option with a required argument
  [
   ;; verbose output
   ["-v" nil "Verbosity level"
    :id :verbosity
    :default 0
    :assoc-fn (fn [m k _] (update-in m [k] inc))]

   ;; A boolean option defaulting to nil
   ["-h" "--help"]])
