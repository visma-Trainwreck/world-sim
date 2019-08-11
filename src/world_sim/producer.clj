(ns world-sim.producer
  (:require [world-sim.tools :as tools]))

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
        (do
          (tools/lock-set (:locks entity-class) entity-key (:name action))
          (tools/events-queue-add world {:entity-id    entity-key
                                         :action       action
                                         :lock         (:locks entity-class)
                                         :entity-class entity-class}))))))

(defn start-job
  [world]
  (gather-events world (get-in world [:gaia :trees])))


