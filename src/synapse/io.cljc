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



(defn on-stdin-by-line [f-line f-end]
  #?(:clj
     (let [results (atom [])]
       (doseq [line (line-seq (java.io.BufferedReader. *in*))]
         (let [r (f-line line)]
           (swap! results conj r)))
       (f-end @results))

     :cljs
     (let [rd (nodejs/require "readline")
           rl (.createInterface rd (clj->js {:input  (.-stdin  js/process)
                                             :output (.-stdout js/process)}))
           results (atom [])]
       (doto rl
         (.setPrompt "")
         (.on "line"  (fn [line]
                        (let [r (f-line line)]
                          (swap! results conj r))
                        (.prompt rl)))
         (.on "close" (fn [] (f-end @results)))
         (.prompt)))))



(defn show-message [& ms]
  #?(:clj  (.println (System/err) (str/join " " (map str ms)))
     :cljs (.write   (.-stderr nodejs/process)
                     (str (str/join " " (map str ms)) "\n"))))



(defn exit [status msg]
  (when msg
    (show-message msg))
  #?(:clj  (System/exit status)
     :cljs (.exit nodejs/process status)))
