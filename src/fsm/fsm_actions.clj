(ns fsm.fsm-actions)

(defn sleep
  [fsm]
  (println "SLEEEEPING")
  (let [entity (:tilakone.core/process fsm)
        energy (:energy (:life-stats entity))]
    (assoc-in fsm [:tilakone.core/process :life-stats :energy] (+ energy 1))))