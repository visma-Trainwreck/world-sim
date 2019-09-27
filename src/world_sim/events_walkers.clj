(ns world-sim.events-walkers
  (:require [tilakone.core :as tk]
            [world-sim.tools :refer [return-as-map]]
            [world-sim.tools :as tools]))

#_(defn sleep
  [{:keys [world entity-class entity]}]
  (let [entity-new (assoc-in entity [:life-stats :energy]
                             (+ 1 (get-in entity [:life-stats :energy])))]
    {:entity-class entity-class
     :entity-new entity-new
     :opt :add
     :func-return nil}))

(defn use-fsm
  [world entity-class entity-id _]
  (println "RUNNING THE FSM!!!!!")
  (let [entity (entity-id @(:pool entity-class))
        entity-new (-> entity
                       (tk/apply-signal (return-as-map world entity-class entity-id)))]
    {:entity-class entity-class
     :entity-new entity-new
     :opt :add
     :func-return nil}))


