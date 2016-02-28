(ns synapse.io
  #?(:clj
     (:require [clojure.java.io :as io]
               [clojure.string :as str])
     :cljs
     (:require [cljs.nodejs :as nodejs]
               [clojure.string :as str])))


(defn result-or-error
  "unifies Exception handling between Cljoure and Javascript
   if the thunk operation succeed returns [value nil].
   it the thunk operation fails returns [nil error]"
  {:style/indent 1}
  [message thunk]
  #?(:clj
     (try
       [(thunk) nil]
       (catch Exception x
         [nil (ex-info message {:error message :reason (.getMessage x) :cause x})]))

     :cljs
     (try
       [(thunk) nil]
       (catch :default x
         [nil (ex-info message {:error message :reason (.-message x) :cause x})]))))



(defn try-or-die
  "unifies Exception handling between Cljoure and Javascript"
  {:style/indent 1}
  [message thunk]
  (let [[result error] (result-or-error message thunk)]
    (if error
      (throw error)
      result)))


(defn environment-map []
  (into {}
        #?(:clj  (System/getenv)
           :cljs (let [env (.-env nodejs/process)]
                   (map (fn [k] [k (aget env k)])
                        (.keys js/Object env))))))


(defn read-file-or-error [file]
  (result-or-error (str "ERROR: Unable to read file: " file)
    (fn []
      #?(:clj
         (slurp file)

         :cljs
         (let [fs (nodejs/require "fs")]
           (.readFileSync fs file "utf8"))))))


(defn read-file [file]
  (let [[result error] (read-file-or-error file)]
    (if error
      (throw error)
      result)))


(defn write-file-or-error [file content]
  (result-or-error (str "ERROR: Unable to write file: " file)
    (fn []
      #?(:clj  (spit file content)
         :cljs (let [fs (nodejs/require "fs")]
                 (.writeFileSync fs file content "utf8"))))))


(defn write-file [file content]
  (let [[result error] (write-file-or-error file content)]
    (if error
      (throw error)
      result)))



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
