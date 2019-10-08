(ns world-sim.consumer
  (:require [world-sim.tools :as tools]
            [world-sim.logs :as logs])
  (:import [java.util.concurrent Executors Future ThreadPoolExecutor ExecutorService]))

(defn execute-events-new
  [world action {:keys [entity-id entity-class]}]
  (if (get-in world [:system :logging-all])
    (logs/store-log {:world        world
                     :entity-class entity-class
                     :entity       entity-id
                     :action       action
                     :type         :consumer}))
  (try (loop [events-list (:events action)
              status true]
         (if (and status (first events-list))
           (recur
             (rest events-list)
             (tools/input world entity-class entity-id (first events-list) status))))
       (catch Exception e
         (if (or
               (get-in world [:system :logging-all])
               (get-in world [:system :logging-exceptions]))

           (logs/store-log {:world        world
                            :entity-class entity-class
                            :entity       entity-id
                            :action       action
                            :exception    e})))))

#_(defn execute-events
    [world events fn-map]
    (let [{:keys [entity-id entity-class]} fn-map]
      (doseq [event (:events events)]
        (event world entity-class entity-id))))

(defn gather-event
  [world]
  (let [action-map (tools/events-queue-pop world)
        {:keys [lock action entity-id entity-class]} action-map]
    (if entity-id
      (do
        (execute-events-new world action {:entity-id    entity-id
                                          :entity-class entity-class})
        (tools/release-lock lock entity-id (:name action))))))

(defn start-job
  [world]
  (try (future (println "consumer started!")
               (while true (get-in world [:system :main-running?])
                 (gather-event world))
               (println "consumer stopped!"))
       (catch Exception e
         (println e))))

