(ns synapse.core
  (:refer-clojure :exclude [resolve])
  (:require [synapse.env :as env]
            [synapse.docker :as docker]
            [synapse.parser :refer [parse]]
            [clojure.string :as str]))


(defmulti resolve (fn [_ lnk] (:resolver lnk)))


(defmethod resolve :env
  [env-map {:keys [link-type target]
            :or {link-type :single}}]
  (let [vars (map second (env/candidates env-map target))]
    (if (= link-type :multiple)
      (str/join "," vars)
      (first (sort vars)))))



(defmethod resolve :docker
  [env-map {:keys [link-type target port]
            :or {link-type :single} :as o}]
  (let [links (if port
                (docker/candidates-links env-map target port)
                (docker/candidates-links env-map target))
        endpoints (map (fn [{:keys [address port]}] (str address ":" port)) links)]
    (if (= link-type :multiple)
      (str/join "," endpoints)
      (first (sort endpoints)))))



(defn resolvables
  [text]
  (->> text
       (re-seq #"%%([^%]+)%%")
       (map second)))


(defn resolve-all [env-map resolvables]
  (->> resolvables
       (map (juxt identity (comp (partial resolve env-map) parse)))
       (into {})))


(defn template-replace-all [template resolvable-map]
  (let [qr #(str/re-quote-replacement (str "%%" % "%%"))] ;; TODO: %% or not?
    (reduce (fn [t [k v]] (str/replace t (qr k) v))
       template resolvable-map)))


(defn resolve-template [env-map template]
  (let [to-resolve (resolvables template)
        resolve-map (resolve-all env-map to-resolve)]
    (template-replace-all template resolve-map)))


(comment
  (defn env-from-file [file]
    (->> file
         slurp
         str/split-lines
         (map #(str/split % #"="))
         (filter #(== 2 (count %)))
         (into {})))


  (def env-map (env-from-file "/tmp/env.sample"))

  (resolve env-map {:resolver :docker :target "zookeeper.*" :link-type :multiple} )

  (docker/candidates-links env "zookeeper.*")

  (def template
    "from: %%HOME%% -> [%%>>zookeeper.*%%]")

  (resolve-template
   env-map
   template)


  )
