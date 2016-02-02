(ns synapse.parser
  (:require [instaparse.core :as insta]
            [clojure.java.io :as io]))



(defn parser []
  (insta/parser (io/resource "parser-grammar.ebnf")))



(->> ((parser) ">>zookeeper.*:2181")
     (insta/transform
      {:port
       (fn [p] {:port p})

       :target
       (fn [t _ port]
         (merge {:target t } port))

       :link_type
       (fn [[t]]
         {:link-type
          (case t :multiple_link :multiple :single_link :single)})

       :docker_spec
       (fn [_ & args]
         (if (= "docker" (first args))
           (apply merge {:resolver :docker} (rest args))
           (apply merge {:resolver :docker} args)))

       :spec
       identity}))


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

      :link_type
      (fn [[t]]
        {:link-type
         (case t :multiple_link :multiple :single_link :single)})

      :docker_spec
      (fn [& args]
        (apply merge {:resolver :docker} args))

      :env_spec
      (fn [& args]
        (apply merge {:resolver :env :link-type :single} args))

      :spec
      identity}))


(defn parse [spec]
  (let [out ((parser) spec)]
    (tree-walk-transform out)))


(comment

  (parse ">>zookeeper.*:2181")
  (-> ((parser) "R23"))
  (parse "env>>R23.*")


  )