(ns user
  (:refer-clojure :exclude [resolve])
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



(comment

  ;; generate platform compatibility test

  (def env
    {"SIMPLE_VAR" "simple-value"
     "MULTI_VAR1" "multiple"
     "MULTI_VAR2" "env-var"
     "MULTI_VAR3" "matched"

     "SINGLE_PORT_3306_TCP" "tcp://172.17.1.10:24123"

     "MORE_PORTS_PORT_9100_TCP"      "tcp://172.17.2.10:24123"
     "MORE_PORTS_PORT_9200_TCP_ADDR" "172.17.2.10"
     "MORE_PORTS_PORT_9200_TCP_PORT" "123"
     "MORE_PORTS_PORT_9300_TCP"      "tcp://172.17.2.10:34123"
     "MORE_PORTS_PORT_9300_TCP_ADDR" "172.17.2.10"
     "MORE_PORTS_PORT_9300_TCP_PORT" "34123"

     "MULTIPLE_1_PORT_9100_TCP"      "tcp://172.17.3.10:9100"
     "MULTIPLE_1_PORT_9200_TCP"      "tcp://172.17.3.10:9200"
     "MULTIPLE_1_PORT_9300_TCP"      "tcp://172.17.3.10:9300"
     "MULTIPLE2_PORT_9100_TCP"       "tcp://172.17.3.20:9100"
     "MULTIPLE2_PORT_9200_TCP"       "tcp://172.17.3.20:9200"
     "MULTIPLE2_PORT_9300_TCP"       "tcp://172.17.3.20:9300"
     "MULTIPLE_A_PORT_9100_TCP"     "tcp://172.17.3.30:9100"
     "MULTIPLE_A_PORT_9200_TCP"     "tcp://172.17.3.30:9200"
     "MULTIPLE_A_PORT_9300_TCP"     "tcp://172.17.3.30:9300"
     })

  (def output
    (->> (io/read-file "./dev-resources/compatibility-test.txt.tmpl")
         (resolve-template env)
         :output))

  ;; create test env
  (->> env
       (sort-by first)
       (map (fn [[k v]] (str "export " k "='" v "'")))
       (str/join "\n")
       (str "#!/bin/bash\n\n# compatibility test env\n\n")
       (spit "../synapse-cli/bin/compat-test-env.sh"))

  ;; create expected output
  (spit "../synapse-cli/bin/compat-test-expected.txt" output)
  )
