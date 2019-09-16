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

(defn sleep-here?
  [world entity-class entity]
  true)