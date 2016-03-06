(ns synapse.util
  #?(:clj
     (:require [clojure.string :as str])

     :cljs
     (:require [clojure.string :as str]
               [goog.string :as gstring]
               [goog.string.format])))

#?(:cljs
   ;; format is not available in clojurescript yet
   (defn format
     "Formats a string using goog.string.format."
     [fmt & args]
     (apply gstring/format fmt args)))


(defn- list-template-error
  [& errs]
  (let [max-size (reduce max (map (comp count first) errs))]
    (->> errs
         (map (fn [[k {:keys [error sources]}]]
                (cond
                  (and (nil? error) (empty? sources))
                  (format (str "%-" max-size "s --> Couldn't be resolved.") k)

                  (and (= :parsing error))
                  (format (str "%-" max-size "s --> Invalid resolvable tag. "
                               "Please check help page.") k)

                  :else
                  (format (str "%-" max-size "s --> Unrecognized error.") k))))
         (str/join "\n"))))



(defn- prepend-file-name [file message]
  (-> message
      (str/replace #"^" (str file ": "))
      (str/replace #"\n" (str "\n" file ": "))))


(defn pretty-print-resolution-errors [result]
  (let [errors   (-> result :resolutions :fail)
        display (apply list-template-error errors)]
    display))


(defn pretty-print-exception [exception]
  (let [{:keys [error reason]} (ex-data exception)]
    (str "" error ", reason: " reason)))


(defn pretty-print-errors [result & {:keys [file]}]
  (let [display (case (:resolution result)
                  :ok ""
                  :with-errors (pretty-print-resolution-errors result)
                  :error (pretty-print-exception (:error result)))]
    (if file
      (prepend-file-name file display)
      display)))
