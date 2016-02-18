(ns synapse.main
  (:require [synapse.cli :refer [-main-cmd-line]])
  #?(:clj (:gen-class)))

(defn -main [& args]
  (apply -main-cmd-line args))

#?(:cljs
   (do
     (enable-console-print!)
     (set! *main-cli-fn* -main)))
