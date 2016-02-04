(ns synapse.parser-test
  (:require [synapse.parser :refer :all]
            [midje.sweet :refer :all]))



(fact "env spec parsing"

      (parse "HOME")
      => {:resolver :env, :link-type :single, :target "HOME"}

      (parse "ENV_VAR")
      => {:resolver :env, :link-type :single, :target "ENV_VAR"}

      (parse "env>ENV_VAR12")
      => {:resolver :env, :link-type :single, :target "ENV_VAR12"}

      (parse "env>>zookeeper.*")
      => {:resolver :env, :link-type :multiple, :target "zookeeper.*"}

      (parse "%%ENV_VAR%%")
      => {:resolver :env, :link-type :single, :target "ENV_VAR"}

      (parse "%%env>ENV_VAR12%%")
      => {:resolver :env, :link-type :single, :target "ENV_VAR12"}

      (parse "%%env>>zookeeper.*%%")
      => {:resolver :env, :link-type :multiple, :target "zookeeper.*"}

      )



(fact "docker spec parsing"

      (parse ">zookeeper")
      => {:resolver :docker, :link-type :single, :target "zookeeper"}

      (parse ">zookeeper:2181")
      => {:resolver :docker, :link-type :single, :target "zookeeper", :port "2181"}

      (parse "docker>zookeeper:2181")
      => {:resolver :docker, :link-type :single, :target "zookeeper", :port "2181"}

      (parse ">>zookeeper.*")
      => {:resolver :docker, :link-type :multiple, :target "zookeeper.*"}

      (parse ">>zookeeper:2181")
      => {:resolver :docker, :link-type :multiple, :target "zookeeper", :port "2181"}

      (parse "docker>>zookeeper:2181")
      => {:resolver :docker, :link-type :multiple, :target "zookeeper", :port "2181"}

      (parse ">>zookeeper.*:2181")
      => {:resolver :docker, :link-type :multiple, :target "zookeeper.*", :port "2181"}

      (parse "%%docker>>zookeeper:2181%%")
      => {:resolver :docker, :link-type :multiple, :target "zookeeper", :port "2181"}

      (parse "%%>>zookeeper.*:2181%%")
      => {:resolver :docker, :link-type :multiple, :target "zookeeper.*", :port "2181"}

      )
