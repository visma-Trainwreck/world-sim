(ns fsm.fsm-actions
  (:require [path-finder.tools :as pf-tools]
            [path-finder.a-star :as pf-core]))

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
  (println "GETTING PATH")
  (let [world (get-in fsm [:tilakone.core/signal :world])
        ;;todo this should be dynamic food not always grass
        goal-key (get-in fsm [:tilakone.core/process :stats :current-goal])
        tile-start (get-in fsm [:tilakone.core/process :location :tile-id])
        tile-goal (rand-nth (pf-tools/find-nearest tile-start goal-key world))
        _ (println "Tile start " tile-start " Tile Goal " tile-goal)
        path (pf-core/get-path world tile-start tile-goal)]
    (println "PRINTING PATH " path)
    (assoc-in fsm [:tilakone.core/process :stats :current-path] path)))

(defn travel-path
  "change the tile its on, its path and its direction?"
  [fsm]
  (let [path (get-in fsm [:tilakone.core/process :stats :current-path])
        _         (println "Travel the path: " path)
        next-tile (first path)
        new-path (drop 1 path)]
    (-> fsm
        (assoc-in[:tilakone.core/process :stats :current-path] new-path)
        (assoc-in[:tilakone.core/process :location] {:x (first next-tile) :y (second next-tile) :tile-id next-tile}))))

(defn unset-goal
  [fsm]
  (assoc-in fsm [:tilakone.core/process :stats :current-goal] nil))

(defn set-goal
  [fsm goal]
  (assoc-in fsm [:tilakone.core/process :stats :current-goal] goal))