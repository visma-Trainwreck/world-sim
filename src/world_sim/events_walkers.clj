(ns world-sim.events-walkers
  (:require [tilakone.core :as tk]))

(defn use-fsm
  [world entity-class entity-id _]
  (let [_   (println entity-id "entity-id")
        entity (entity-id @(:pool entity-class))
        entity-new (-> entity
                       :fsm
                       (tk/apply-signal "")
                       (as-> fsm (assoc entity :fsm fsm)))]
    {:entity-class entity-class
     :entity-new entity-new
     :opt :add
     :func-return nil}))


