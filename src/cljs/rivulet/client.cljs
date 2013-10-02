(ns rivulet.client
  (:require [enfocus.core :as ef]
            [enfocus.events :as events]
            [vertx.client.eventbus :as eb]
            [rivulet.config :refer [config]]))

(defn endpoint []
  (str (.-protocol js/location) "//"
       (.-host js/location)
       "/eventbus"))

(defn open-eventbus
  "Opens a connection to the remote EventBus endpoint.
   See web.clj to see how sockjs_endpoint is injected."
  [on-open]
  (let [eb (eb/eventbus (endpoint))]
    (eb/on-open eb (fn [_] (.log js/console "eventbus opened")))
    (eb/on-open eb on-open)))

(defn send-command [eb command payload]
  (eb/publish eb (:command-address config)
              {:command command :payload payload}))

(defn filter-selector [filter]
  (str "div.filter[data-filter=\"" filter "\"]"))

(defn delete-filter [eb filter]
  (send-command eb "delete-filter" filter)
  (ef/at (filter-selector filter) (ef/remove-node)))

(defn on-click [id f]
  (ef/at id (events/listen :click f)))

(defn add-html [loc id html]
  (ef/at id ((if (= :append loc) ef/append ef/prepend) (ef/html html))))

(def append-html (partial add-html :append))

(def prepend-html (partial add-html :prepend))

(defn add-filter [eb filter]
  (append-html "#filters"
               [:div {:class "filter"
                      :data-filter filter}
                [:span {:class "title"} filter]
                " "
                [:button "Delete"]
                [:div {:class "results"}]])
  (on-click (str (filter-selector filter) " button") #(delete-filter eb filter))
  (send-command eb "add-filter" filter))

(defn result-listener [[filter result]]
  (prepend-html (str (filter-selector filter) " div.results")
                [:div result]))

(defn attach-result-listener
  [eb]
  (eb/on-message eb (:result-address config) result-listener))

(let [raw-handler (atom nil)]
  (defn toggle-raw-stream [eb]
    (if @raw-handler
      (do
        (ef/at "#raw-stream" (ef/remove-node))
        (eb/unregister-handler @raw-handler)
        (reset! raw-handler nil))
      (do
        (append-html "#raw-wrapper" [:div {:id "raw-stream"}])
        (reset! raw-handler (eb/on-message eb (:stream-address config)
                                           #(prepend-html "#raw-stream" [:div %])))))))

(defn init []
  (open-eventbus
   (fn [eb]
     (attach-result-listener eb)
     (on-click "#add-filter" #(add-filter eb
                                          (ef/from "#filter" (ef/get-prop :value))))
     (on-click "#toggle-raw" #(toggle-raw-stream eb)))))

(set! (.-onload js/window) init)
