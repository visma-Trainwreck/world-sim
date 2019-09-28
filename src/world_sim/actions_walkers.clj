(ns world-sim.actions-walkers
  (:require [world-sim.events-walkers :as walker-events]))

(def entity-fsm
  {:name      :use-fsm
   :condition (fn [_ _ entity] true #_(if (get-in entity [:plan :current-goal])
                                        true
                                        false))
   :events    [walker-events/use-fsm]})

#_(def entity-grow
  {:name      :grow
   :condition (fn [world entity-class entity]
                (let [max-size (:base-size entity-class)
                      entity-size (:size entity)]
                  (and
                    (not (:death-date entity))
                    (> max-size entity-size))))
   :events    [events/entity-grow]})

(defn enqueue
  [entity goal]
  (-> (get-in entity [:mind-map :current-goal])
      (conj goal)
      (as-> goals
            (assoc-in entity [:mind-map :current-goal] goals))))

#_(defn goals
  [world entity-class entity]
  (if (empty? (get-in entity [:mind-map :current-goal]))
    (cond entity
          (< (:hunger entity) 10) (enqueue entity :eat))))

#_(def mind-map
  {:current-goal [:travel :eat]
   :current-path [[1 1] [2 2] [3 2]]
   :calc-goal    (fn [world entity-class entity]
                   (cond entity
                         (< (:hunger entity) 10) (enqueue entity :eat)))})

