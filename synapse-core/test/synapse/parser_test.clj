(ns synapse.parser-test
  (:require [synapse.parser :refer :all]
            [midje.sweet :refer :all]))



(fact "env spec parsing"

      (parse "%%HOME%%")
      => {:resolver :env, :link-type :single, :target "HOME"}

      (parse "%%ENV_VAR%%")
      => {:resolver :env, :link-type :single, :target "ENV_VAR"}

      (parse "%%env>ENV_VAR12%%")
      => {:resolver :env, :link-type :single, :target "ENV_VAR12"}

      (parse "%%env>>zookeeper.*%%")
      => {:resolver :env, :link-type :multiple, :target "zookeeper.*"}

      (parse "%%ENV_VAR%%")
      => {:resolver :env, :link-type :single, :target "ENV_VAR"}

      (parse "%%env>ENV_VAR12%%")
      => {:resolver :env, :link-type :single, :target "ENV_VAR12"}

      (parse "%%env>>zookeeper.*%%")
      => {:resolver :env, :link-type :multiple, :target "zookeeper.*"}

      )



(fact "docker spec parsing"

      (parse "%%>zookeeper%%")
      => {:resolver :docker, :link-type :single, :target "zookeeper"}

      (parse "%%>zookeeper:2181%%")
      => {:resolver :docker, :link-type :single, :target "zookeeper", :port "2181"}

      (parse "%%docker>zookeeper:2181%%")
      => {:resolver :docker, :link-type :single, :target "zookeeper", :port "2181"}

      (parse "%%>>zookeeper.*%%")
      => {:resolver :docker, :link-type :multiple, :target "zookeeper.*"}

      (parse "%%>>zookeeper:2181%%")
      => {:resolver :docker, :link-type :multiple, :target "zookeeper", :port "2181"}

      (parse "%%docker>>zookeeper:2181%%")
      => {:resolver :docker, :link-type :multiple, :target "zookeeper", :port "2181"}

      (parse "%%>>zookeeper.*:2181%%")
      => {:resolver :docker, :link-type :multiple, :target "zookeeper.*", :port "2181"}

      (parse "%%docker>>zookeeper:2181%%")
      => {:resolver :docker, :link-type :multiple, :target "zookeeper", :port "2181"}

      (parse "%%>>zookeeper.*:2181%%")
      => {:resolver :docker, :link-type :multiple, :target "zookeeper.*", :port "2181"}

      )


(fact "prefix spec parsing"

      (parse "%%prefix>KAFKA_%%")
      => {:resolver :prefix, :target "KAFKA_"}

      (parse "%%prefix[sep=.,case=lower,type=properties]>KAFKA_%%")
      => {:resolver :prefix, :options {:separator ".", :case :lower, :type :properties}, :target "KAFKA_"}

      (parse "%%prefix>ZK_%%")
      => {:resolver :prefix, :target "ZK_"}

      (parse "%%prefix[sep=,case=camel,type=properties]>ZK_%%")
      => {:resolver :prefix, :options {:separator "", :case :camel, :type :properties}, :target "ZK_"}
      )


(facts "testing parser errors"

       (-> "%%>>>%%" parse :error) => :parsing
       (-> "%%%%" parse :error)    => :parsing
       (-> "%%" parse :error)      => :parsing
       (-> "%" parse :error)       => :parsing
       (-> "" parse :error)        => :parsing


       (-> "%%docker[>>zoo%%" parse :error)          => :parsing
       (-> "%%docker[]>>zoo%%" parse :error)         => :parsing
       (-> "%%docker]>>zoo%%" parse :error)          => :parsing
       (-> "%%docker[blah]>>zoo%%" parse :error)     => :parsing
       (-> "%%docker[addr;boo]>>zoo%%" parse :error) => :parsing

       (-> "%%prefix[case=foo,type=properties]>KAFKA_%%" parse :error) => :parsing
       (-> "%%prefix[type=bar]>KAFKA_%%" parse :error) => :parsing

       )



(facts "testing options: addr and port"

       (-> (parse "%%[addr]>>zookeeper.*:2181%%") :options)
       => {:addr true}

       (-> (parse "%%docker[addr]>>zookeeper.*:2181%%") :options)
       => {:addr true}

       (-> (parse "%%[addr,port]>>zookeeper.*:2181%%") :options)
       => {:addr true :port true}

       (-> (parse "%%docker[addr,port]>>zookeeper.*:2181%%") :options)
       => {:addr true :port true}

       )



(facts "testing options: separator"

       (-> (parse "%%[sep=;]>>zookeeper.*:2181%%") :options)
       => {:separator ";"}

       (-> (parse "%%[sep=any]>>zookeeper.*:2181%%") :options)
       => {:separator "any"}

       (-> (parse "%%[sep=]>>zookeeper.*:2181%%") :options)
       => {:separator ""}

       (-> (parse "%%[sep=|]>>zookeeper.*:2181%%") :options)
       => {:separator "|"}

       (-> (parse "%%[sep=\n]>>zookeeper.*:2181%%") :options)
       => {:separator "\n"}

       ;; \n and \t are un-escaped
       (-> (parse "%%[sep=\\n]>>zookeeper.*:2181%%") :options)
       => {:separator "\n"}

       (-> (parse "%%[sep=\\t]>>zookeeper.*:2181%%") :options)
       => {:separator "\t"}

       ;; comma is not support as it is the default
       (-> (parse "%%[sep=,addr]>>zookeeper.*:2181%%") :options)
       => {:separator "" :addr true}

       (-> (parse "%%env[sep=:]>>zookeeper.*%%") :options)
       => {:separator ":"}

       )


(facts "testing default value"

       (-> (parse "%%>>zookeeper.*:2181||default value%%") :default)
       => "default value"

       (-> (parse "%%docker[addr]>>zookeeper.*:2181||default value%%") :default)
       => "default value"

       (-> (parse "%%>>zookeeper.*:2181||1%%") :default)
       => "1"

       (-> (parse "%%>>zookeeper.*:2181||1:2%%") :default)
       => "1:2"

       (-> (parse "%%env>DATA_DIR||/data%%") :default)
       => "/data"

       (-> (parse "%%LOGS_DIR||/logs%%") :default)
       => "/logs"

       (-> (parse "%%LOGS_DIR||%%") :default)
       => ""

       (-> (parse "%%>>zookeeper.*:2181||%%") :default)
       => ""
       )
