(ns synapse.parser
  (:require [instaparse.core :as insta]
            [clojure.java.io :as io]))



(defn parser []
  (insta/parser (io/resource "parser-grammar.ebnf")))


(def tree-walk-transform
  (partial insta/transform
     {:port
      (fn [p] {:port p})

      :target
      (fn
        ([t]
         {:target t})
        ([t _ port]
         (merge {:target t } port)))

      :addr-opt
      (fn [] {:addr true})

      :port-opt
      (fn [] {:port true})

      :option
      identity

      :options
      (fn [& opts]
        {:options
         (apply merge opts)})

      :default-value
      (fn [v] {:default v})

      :link-type
      (fn [[t]]
        {:link-type
         (case t :multiple-link :multiple :single-link :single)})

      :docker-spec
      (fn [& args]
        (apply merge {:resolver :docker} args))

      :env-spec
      (fn [& args]
        (apply merge {:resolver :env :link-type :single} args))

      :spec
      merge}))


(defn parse [spec]
  (let [result ((parser) spec)]
    (if (insta/failure? result)
      {:resolver :error :error :parsing :reason (insta/get-failure result)}
      (tree-walk-transform result))))


(comment

  (parse "%%>>zookeeper.*:2181%%")
  (-> ((parser) "%%R23%%"))
  (-> ((parser) "%%R23|/opt%%"))
  (-> (parse "%%R23|/opt%%"))
  (-> ((parser) "%%>>zookeeper.*:2181%%"))
  (-> ((parser) "%%[addr,port]>>zookeeper.*:2181|some default value%%"))
  (-> (parse "%%[addr,port]>>zookeeper.*:2181|some default%%"))
  (parse "%%env>>R23.*%%")

  (parse ">>>somethsoidhfa>>>asodifaoiwher")
  )
