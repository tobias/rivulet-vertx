(ns rivulet.init
  (:require [vertx.repl :as repl]
            [vertx.core :as core]
            [rivulet.config :refer [config]]
            [rivulet.filter :as filter]))

(defn init []
  (repl/start-repl 0)
  (filter/init config)
  (core/deploy-verticle "data_source.rb" config)
  (core/deploy-verticle "web.js" config 2))
