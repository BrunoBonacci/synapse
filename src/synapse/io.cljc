(ns synapse.io
  (:require #?(:clj  [clojure.java.io :as io]
               :cljs [cljs.nodejs :as nodejs])
            [clojure.string :as str]))


(defn environment-map []
  (into {}
        #?(:clj  (System/getenv)
           :cljs (let [env (.-env nodejs/process)]
                   (map (fn [k] [k (aget env k)])
                        (.keys js/Object env))))))


(defn read-file [file]
  #?(:clj  (slurp file)
     :cljs (let [fs (nodejs/require "fs")]
             (.readFileSync fs file "utf8"))))


(defn write-file [file content]
  #?(:clj  (spit file content)
     :cljs (let [fs (nodejs/require "fs")]
             (.writeFileSync fs file content "utf8"))))


;; TODO: fix this for cljs
(defn read-resource-file [file]
  ;;(slurp (io/resource file))
  )

;; TODO: fix this for cljs
(defn read-stdin-by-line []
  ;;(line-seq (java.io.BufferedReader. *in*))
  [])


(defn read-stdin-all []
  (str/join "\n" (read-stdin-by-line)))


(defn show-message [& ms]
  #?(:clj  (.println (System/err) (str/join " " (map str ms)))
     :cljs (.write   (.-stderr nodejs/process) (str/join " " (map str ms) "\n"))))


(defn exit [status msg]
  (when msg
    (show-message msg))
  #?(:clj  (System/exit status)
     :cljs (.exit nodejs/process status)))
