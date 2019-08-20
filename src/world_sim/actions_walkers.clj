(ns world-sim.actions-walkers
  (:require [world-sim.mock-maps :as maps]))

(def make-goal
  :name :entity-goal
  :condition (fn [_ _ _] false)
  :events [;;is hungry, is sleepy, is hurt, is alone
  ])

(defn enqueue
  [entity goal]
  (-> (get-in entity [:mind-map :current-goal])
      (conj goal)
      (as-> goals
            (assoc-in entity [:mind-map :current-goal] goals))))

(defn goals
  [world entity-class entity]
  (if (empty? (get-in entity [:mind-map :current-goal]))
    (cond entity
          (< (:hunger entity) 10) (enqueue entity :eat))))

(def mind-map
  {:current-goal [:travel :eat]
   :current-path [[1 1] [2 2] [3 2]]
   :calc-goal    (fn [world entity-class entity]
                   (cond entity
                         (< (:hunger entity) 10) (enqueue entity :eat)))})

