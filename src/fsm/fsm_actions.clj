(ns fsm.fsm-actions)

(defn sleep
  [fsm]
  (println "SLEEEEPING")
  (let [entity (:tilakone.core/process fsm)
        energy (:energy (:life-stats entity))]
    (assoc-in fsm [:tilakone.core/process :life-stats :energy] (+ energy 1))))

(defn eat
  [fsm]
  (println "eating")
  (let [entity (:tilakone.core/process fsm)
        food (get-in entity [:life-stats :food])]
    (assoc-in fsm [:tilakone.core/process :life-stats :food] (+ food 1))))

(defn get-path
  [fsm]
  fsm)

(defn unset-goal
  [fsm]
  (assoc-in fsm [:tilakone.core/process :stats :current-goal] nil))

(defn set-goal
  [fsm goal]
  (assoc-in fsm [:tilakone.core/process :stats :current-goal] goal))