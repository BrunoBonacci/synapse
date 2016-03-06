(ns user
  (:require [synapse.core :refer :all]
            [synapse.io :as io]
            [clojure.string :as str]))

(comment

  ;; generate cheatsheet

  (->
   (resolve-template
    {"HOME" "/home/ubuntu"
     "ALLOWED_IP1" "172.17.0.21"
     "ALLOWED_IP2" "172.17.1.2"
     "ALLOWED_IP3" "172.17.3.45"
     "DB_PORT_3306_TCP" "tcp://172.17.12.21:12321"
     "ELS1_PORT_9200_TCP" "tcp://172.17.15.10:24123"
     "ELS1_PORT_9300_TCP" "tcp://172.17.15.10:27422"
     "ELS2_PORT_9200_TCP" "tcp://172.17.15.20:12433"
     "ELS2_PORT_9300_TCP" "tcp://172.17.15.20:11322"
     "ELS3_PORT_9200_TCP" "tcp://172.17.15.30:9413"
     "ELS3_PORT_9300_TCP" "tcp://172.17.15.30:10112"
     }
    (io/read-file "./dev-resources/cheatsheet.md.tmpl"))
   :output
   (str/replace #"\$" "%"))

  )
