(ns world-sim.world-loop
  (:require [world-sim.mock-maps :as mocks]))

(def is-running (ref false))






(defn run
  []
  (loop [world mocks/world
         i (deref (get-in world [:physics :time]))]
    (if-not (#{300} i)
      (do
        (Thread/sleep 500)
        (swap! (fn [] (get-in world [:physics :time])) i)
        (recur (simulate world) (inc i)))
      (do
        (println "done")
        (Thread/sleep 1000)
        (swap! is-running (fn [_] false))))))
