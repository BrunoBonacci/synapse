(ns synapse.env
  (:require [clojure.string :as str]))


(comment
  (defn env-from-file [file]
    (->> file
         slurp
         str/split-lines
         (map #(str/split % #"="))
         (filter #(== 2 (count %)))
         (into {})))


  (def env (env-from-file "/tmp/env.sample")))


(defn candidates [env target]
  (filter
   #(re-matches (re-pattern (str "(?i)" target)) (first %))
   env))

;;(candidates env "els.*")


(defn property-name-tranformer
  "returns a function which given an ENVIRONMENT variable name, it returns a property name
  with the same name but with the defined separator and defined case

  for exmaple:
  with `{:separator \".\" :case :lower}`
  \"FOO_BAR_BAZ\" -> \"foo.bar.baz\"

  with `{:separator \"\" :case :camel}`
  \"FOO_BAR_BAZ\" -> \"fooBarBaz\"

  with `{:separator \"-\" :case :camel}`
  \"FOO_bar_BazAndMore\" -> \"FOO-baz-BazAndMore\"

  "
  [{:keys [separator case]
    :or {separator "." case :lower}}]
  (fn [pname]
    (as-> pname $
        (if (= :preserve case) $ (str/lower-case $))
        (str/split $ #"_")
        (if (= :camel case) (cons (first $) (map str/capitalize (rest $))) $)
        (str/join separator $))))
