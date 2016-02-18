(ns synapse.util
  (:require [clojure.string :as str]))


(defn- list-template-error
  [& errs]
  (let [max-size (reduce max (map (comp count first) errs))]
    (->> errs
         (map (fn [[k {:keys [error sources]}]]
                (cond
                  (and (nil? error) (empty? sources))
                  (format (str "%-" max-size "s --> Couldn't be resolved.") k)

                  (and (= :parsing error))
                  (format (str "%-" max-size "s --> Invalid resolvable tag. Please check help page.") k)

                  :else
                  (format (str "%-" max-size "s --> Unrecognized error.") k))))
         (str/join "\n"))))



(defn- prepend-file-name [file message]
  (-> message
      (str/replace #"^" (str file ": "))
      (str/replace #"\n" (str "\n" file ": "))))



(defn pretty-print-errors [errors & {:keys [file]}]
  (let [display (apply list-template-error errors)]
    (if file
      (prepend-file-name file display)
      display)))
