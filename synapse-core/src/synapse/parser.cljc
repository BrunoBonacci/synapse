(ns synapse.parser
  (:require [instaparse.core :as insta]
            [clojure.string :as str]))


(defn- unescape [s]
  (-> s
      (str/replace #"\\n" "\n")
      (str/replace #"\\t" "\t")))


(def grammar
  "
  spec          := <'%%'> (prefix-spec / docker-spec / env-spec) [<'||'> default-value] <'%%'>

  prefix-spec   := <'prefix'> [prefix-options] <'>'> target

  env-spec      := [<'env'> [options] link-type] target

  docker-spec   := [<'docker'>] [options] link-type target

  options       := <'['> option ( <','> option )*  <']'>
  option        := addr-opt / port-opt / sep-opt
  addr-opt      := <'addr'>
  port-opt      := <'port'>
  sep-opt       := <'sep='> #'[^\\],]*'

  prefix-options := <'['> option2 ( <','> option2 )*  <']'>
  option2       := type-opt / case-opt / sep-opt
  case-opt      := <'case='> #'lower|camel|preserve'
  type-opt      := <'type='> #'properties'

  target        := #'[^>:%|]+' [':' port]

  port          := #'[0-9]+'

  link-type     := multiple-link | single-link
  multiple-link := <'>>'>
  single-link   := <'>'>

  default-value := #'[^%]*'

")


(defonce parser (insta/parser grammar))


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

      :sep-opt
      (fn ([] {:separator ""})
        ([sep] {:separator (unescape sep)}))

      :case-opt
      (fn
        [case] {:case (keyword case)})

      :type-opt
      (fn
        [type] {:type (keyword type)})

      :option
      identity

      :option2
      identity

      :options
      (fn [& opts]
        {:options
         (apply merge opts)})

      :prefix-options
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

      :prefix-spec
      (fn [& args]
        (apply merge {:resolver :prefix} args))

      :spec
      merge}))


(defn parse [spec]
  (let [result (parser spec)]
    (if (insta/failure? result)
      {:resolver :error :error :parsing :reason (insta/get-failure result)}
      (tree-walk-transform result))))
