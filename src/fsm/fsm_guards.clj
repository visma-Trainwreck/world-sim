(ns fsm.fsm-guards
  (:require [tilakone.core :as tk]))

(defn go-sleep?
  [{::tk/keys [signal] :as fsm}]
  (let [entity (:tilakone.core/process fsm)
        energy (:energy (:life-stats entity))]
    (if (< energy 2)
      true
      false)))

(defn done-sleep?
  [{::tk/keys [signal] :as fsm}]
  (println "DONE SLEEP ?")
  (let [entity (:tilakone.core/process fsm)
        energy (:energy (:life-stats entity))]
    (if (< energy 2)
      false
      true)))

(defn go-eat?
  [fsm]
  (let [entity (:tilakone.core/process fsm)
        food (:food (:life-stats entity))]
    (if (< food 2)
      true
      false)))

(defn done-eat?
  [fsm]
  (println "DONE SLEEP ?")
  (let [entity (:tilakone.core/process fsm)
        food (:food (:life-stats entity))]
    (if (< food 2)
      false
      true)))

(defn can-eat?
  [fsm]
  (let [entity (:tilakone.core/process fsm)
        tile-id (get-in entity [:location :tile-id])
        tile (tile-id @(get-in fsm [:tilakone.core/signal :world :enviroment :landmasses :pool]))]
    (> (:grass tile) 15)))

(defn has-goal
  [fsm]
  (let [entity (:tilakone.core/process fsm)
        entity-goal (get-in [:stats :current-goa] entity)]
    (nil? entity-goal)))

(defn travel-done?
  [fsm]
  (let [entity (:tilakone.core/process fsm)
        path (get-in entity [:stats :current-path])]
    (empty? path)))