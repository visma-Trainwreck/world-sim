(ns world-sim.consumer
  (:require [world-sim.tools :as tools]))

(defn execute-events-new
  [world action {:keys [entity-id entity-class]}]
  (loop [events-list (:events action)
         status true]
    (if (and status (first events-list))
      (recur
        (rest events-list)
        (tools/input world entity-class entity-id (first events-list) status)))))

#_(defn execute-events
    [world events fn-map]
    (let [{:keys [entity-id entity-class]} fn-map]
      (doseq [event (:events events)]
        (event world entity-class entity-id))))

(defn gather-event
  [world]
  (try
    (let [action-map (tools/events-queue-pop world)
          {:keys [lock action entity-id entity-class]} action-map]
      (if entity-id
        (do
          (execute-events-new world action {:entity-id    entity-id
                                            :entity-class entity-class})
          (tools/release-lock lock entity-id (:name action)))))
    (catch Exception e
      (println e))))

(defn start-job
  [world]
  (future
    (try (println "consumer starter")
         (let [events-ref (get-in world [:system :events-done])]
           (while @(get-in world [:system :main-running?])
             (gather-event world)
             #_(send events-ref (fn [x] (inc x))))
           (println "consumer stopped !"))
         (catch Exception e
           (println e)))))