(ns node-repl)

(comment
  (require 'cljs.repl)
  (require 'cljs.build.api)
  (require 'cljs.repl.node)

  (cljs.build.api/build "src"
                        {:main 'synapse.main
                         :output-to "target/synapse.js"
                         :verbose true})

  (cljs.repl/repl (cljs.repl.node/repl-env)
                  :watch "src"
                  :output-dir "target"))


(comment

  ;; cider clojurescript repl
  (require 'cemerick.piggieback)
  (cemerick.piggieback/cljs-repl (cljs.repl.rhino/repl-env))

  ;;(require 'cljs.repl.node)
  ;;(cemerick.piggieback/cljs-repl (cljs.repl.node/repl-env))

  (+ 1 2)
  )
