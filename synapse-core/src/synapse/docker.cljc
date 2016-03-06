(ns synapse.docker
  (:require [clojure.string :as str]
            [synapse.env :refer [candidates]]))

(comment
  (defn env-from-file [file]
    (->> file
         slurp
         str/split-lines
         (map #(str/split % #"="))
         (filter #(== 2 (count %)))
         (into {})))


  (def env (env-from-file "/tmp/env.sample")))


(defn to-num [n]
  (when (not-empty n)
    (Integer/parseInt n)))


(defn- candidates-links-style1
  [env link port]
  (let [;; finding CONTAINER_1_PORT_12345_TCP_ADDR style
        addrs (candidates env (str ".*" link "_PORT_" port "_TCP_ADDR$"))
        ;; for a matched env var as above
        ;; find a matching CONTAINER_1_PORT_12345_TCP_PORT
        portname #(str/replace % #"_ADDR$" "_PORT")]
    ;; for every matched env var return the candidate links
    (map
     (fn [[k v]]
       {:link-name link :link-port port
        :address v :port (or (to-num (get env (portname k))) port)
        :source {k v, (portname k) (get env (portname k))}})
     addrs)))


(defn- candidates-links-style2
  [env link port]
  (let [;; finding CONTAINER_1_PORT_12345_TCP style
        addrs (candidates env (str ".*" link "_PORT_" port "_TCP$"))]
    ;; for every matched env var return the candidate links
    (map
     (fn [[k v]]
       {:link-name link :link-port port
        :address (->> v (re-find #"tcp://([^:]+):[0-9]+") second)
        :port    (or (to-num (->> v (re-find #"tcp://[^:]+:([0-9]+)") second)) port)
        :source {k v}})
     addrs)))


(defn lowest-link-port [env link]
  (->> (candidates env (str ".*" link  "_PORT_[0-9]+_TCP(_ADDR)?"))
       (map #(->> % first (re-find #"(?i).*_PORT_([0-9]+)_TCP.*") second))
       (filter identity)
       (map to-num)
       sort
       first))


(defn candidates-links
  ([env link]
   (candidates-links env link (lowest-link-port env link)))
  ([env link port]
   (let [style1 (candidates-links-style1 env link port)
         style2 (candidates-links-style2 env link port)
         candidates (concat style1 style2)
         dedup (group-by (juxt :address :port) candidates)]
     (map (fn [[k v]]
            (assoc
             (first v)
             :source (apply merge (map :source v)))) dedup))))


;;(candidates-links env "zookeeper")
