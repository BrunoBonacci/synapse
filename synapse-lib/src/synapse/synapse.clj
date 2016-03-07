(ns synapse.synapse
  (:require [synapse.core :as s])
  (:require [synapse.io :as sio])
  (:require [clojure.edn :as edn]))


(defn- edn-read-string-or-error
  [data]
  (sio/result-or-error "ERROR: Couldn't parse edn."
    (fn []
      (edn/read-string data))))


(defn load-config-file
  "It loads a configuration file in edn format and resolve
   all missing tags using synapse resolvers.
   It returns a vector containing `[ result, error ]`'.
   In case of errors `result` will be `nil`"
  ([config-file]
   (load-config-file (sio/environment-map) config-file))
  ([env-map config-file]
   (let [[template error] (sio/read-file-or-error config-file)]
     (if error
       [nil error]
       (let [result   (s/resolve-template env-map template)]
         (if (not= :ok (:resolution result))
           [nil (ex-info "Tag resolution failed." result)]
           (edn-read-string-or-error (:output result))))))))



(defn load-config-file!
  "It loads a configuration file in edn format and resolve
   all missing tags using synapse resolvers.
   It returns the configuration data or it raises an
   exception."
  ([config-file]
   (load-config-file! (sio/environment-map) config-file))
  ([env-map config-file]
   (let [[result error] (load-config-file env-map config-file)]
     (if error
       (throw error)
       result))))
