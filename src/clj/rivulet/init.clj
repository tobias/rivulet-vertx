(ns rivulet.init
  (:require [vertx.repl :as repl]
            [vertx.core :as core]
            [rivulet.config :refer [config]]
            [rivulet.filter :as filter]
            [rivulet.stats :as stats]))

(defn init []
  (filter/init config)
  (stats/init config)
  (core/deploy-verticle "data_source.rb" config)
  (core/deploy-verticle "web.js" config 2)
  (repl/start-repl 0))
