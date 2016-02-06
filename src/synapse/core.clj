(ns synapse.core
  (:refer-clojure :exclude [resolve])
  (:require [synapse.env :as env]
            [synapse.docker :as docker]
            [synapse.parser :refer [parse]]
            [clojure.string :as str]
            [synapse.io :as io]))


(defmulti resolve-with-meta (fn [_ lnk] (:resolver lnk)))


(defmethod resolve-with-meta :default [_ r]
  {:resolved nil
   :resolution :fail
   :error :unrecognized-form
   :cause r})


(defmethod resolve-with-meta :error [_ r]
  (merge
   {:resolved nil
    :resolution :fail}
   (dissoc r :resolver)))


(defmethod resolve-with-meta :env
  [env-map {:keys [link-type target]
            :or {link-type :single}}]
  (let [candidates (env/candidates env-map target)
        vars       (map second candidates)
        resolved   (when (seq vars)
                     (if (= link-type :multiple)
                       (str/join "," vars)
                       (first (sort vars))))]
    {:resolved resolved
     :resolution (if (seq vars) :ok :fail)
     :sources candidates}))



(defmethod resolve-with-meta :docker
  [env-map {:keys [link-type target port]
            :or {link-type :single} :as o}]
  (let [links (if port
                (docker/candidates-links env-map target port)
                (docker/candidates-links env-map target))
        endpoints (map (fn [{:keys [address port]}] (str address ":" port)) links)
        resolved (when (seq endpoints)
                   (if (= link-type :multiple)
                     (str/join "," endpoints)
                     (first (sort endpoints))))]
    {:resolved resolved
     :resolution (if (seq endpoints) :ok :fail)
     :sources links}))



(defn resolve [env-map resolvable]
  (:resolved
   (resolve-with-meta env-map resolvable)))



(defn resolvables
  [template]
  (set (re-seq #"%%[^%]+%%" template)))



(defn resolve-all-with-meta [env-map resolvables]
  (->> resolvables
       (map (juxt identity (comp (partial resolve-with-meta env-map) parse)))
       (into {})))


(defn resolve-all [env-map resolvables]
  (->> (resolve-all-with-meta env-map resolvables)
       (map (fn [[k v]] [k (:resolved v)]))
       (into {})))


(defn template-replace-all [template resolvable-map]
  (let [qr #(str/re-quote-replacement %)]
    (reduce (fn [t [k v]] (if v (str/replace t (qr k) v) t))
       template resolvable-map)))


(defn resolve-template [env-map template]
  (let [to-resolve (resolvables template)
        resolved (resolve-all-with-meta env-map to-resolve)
        resolved-pairs (map (fn [[k v]] [k (:resolved v)]) resolved)
        output (template-replace-all template resolved-pairs)
        resolutions (group-by (comp :resolution second) resolved)
        outcome (if (seq (:fail resolutions)) :with-errors :ok)]
    {:resolution outcome
     :output output
     :resolutions resolutions}))


(defn outfile-name [in-file]
  (when in-file
    (let [new-file (str/replace in-file #"\.tmpl$" "")]
      (if (= in-file new-file) (str in-file ".out") new-file))))


(defn resolve-file-template [env-map template-file outfile]
  (let [template (io/read-file template-file)
        result   (resolve-template env-map template)]
    (when (= :ok (:resolution result))
      (io/write-file outfile (:output result)))
    result))


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
    "from: %%HOME%% -> [%%>>>zookeeper.*%%]")

  (resolve-template
   env-map
   template)

  (def tmpl "/tmp/config.edn.tmpl")
  (def template (io/read-file tmpl))
  (def env-map (into {} (System/getenv)))
  (resolve-template env-map template)
  (resolve-file-template env-map tmpl (outfile-name tmpl)))
