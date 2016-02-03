(ns synapse.io)


(defn environment-map []
  (System/getenv))


(defn read-file [file]
  (slurp file))


(defn write-file [file content]
  (spit file content))
