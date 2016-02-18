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
