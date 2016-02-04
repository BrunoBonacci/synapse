(ns synapse.io
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))


(defn environment-map []
  (into {} (System/getenv)))


(defn read-file [file]
  (slurp file))


(defn write-file [file content]
  (spit file content))


(defn read-resource-file [file]
  (slurp (io/resource file)))


(defn read-stdin-by-line []
  (line-seq (java.io.BufferedReader. *in*)))


(defn read-stdin-all []
  (str/join "\n" (read-stdin-by-line)))


(defn show-message [& ms]
  (.println (System/err) (str/join " " (map str ms))))

(defn exit [status msg]
  (show-message msg)
  (System/exit status))
