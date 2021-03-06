(ns world-sim.producer
  (:require [world-sim.tools :as tools]
            [clojure.core.async :refer [<!! >!!]]
            [world-sim.mock-maps :as maps]
            [world-sim.logs :as logs]))

(defn gather-events
  [world entity-jack]
  ;; there is a problem here! If the pool in the class only has one tree-class it wont run.

  (let [class-list (:pool entity-jack)]
    (doseq [[entity-class-id entity-class] class-list
            action (:actions entity-jack)]
      (doseq [[entity-key entity] @(:pool entity-class)
              :let [lock (:locks entity-class)]
              :when (and (tools/check-lock lock entity-key (:name action))
                         ((:condition action) world entity-class entity))]
        (try (do
               (tools/lock-set (:locks entity-class) entity-key (:name action))

               (if (get-in world [:system :logging-all])
                 (logs/store-log {:world        world
                                  :entity-class entity-class
                                  :entity       entity
                                  :action       action
                                  :type         :producer}))
               (tools/events-queue-add world {:entity-id    entity-key
                                              :action       action
                                              :lock         (:locks entity-class)
                                              :entity-class entity-class}))
             (catch Exception e
               (if (or
                     (get-in world [:system :logging-all])
                     (get-in world [:system :logging-exceptions]))

                 (logs/store-log {:world        world
                                  :entity-class entity-class
                                  :entity       entity
                                  :action       action
                                  :exception    e}))))))))

(defn put-event-jacks
  [world]
  (let [chan (:event-gather-starter world)
        jacks maps/jacks]
    (doseq [jack jacks
            :when (= 0 (mod (tools/now world) (:time-divisor jack)))]
      (>!! chan jack)))
  world)

(defn producer-job
  [world]
  (while @(get-in world [:system :main-running?])
    (->> (<!! (:event-gather-starter world))
         (gather-events world))))

(defn producer-start-job
  [world amount]
  (doseq [job (range amount)]
    (future (do (println "producer " job " started")
                (producer-job world)
                (println "producer " job " stopped")))))


