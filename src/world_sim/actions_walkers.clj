(ns world-sim.actions-walkers
  (:require [world-sim.mock-maps :as maps]))

(def make-goal
  :name :entity-goal
  :condition (fn [_ _ _] false)
  :events [;;is hungry, is sleepy, is hurt, is alone
  ])






