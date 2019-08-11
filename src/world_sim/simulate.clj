(ns world-sim.simulate
  (:require [world-sim.mock-maps :as maps]
            [world-sim.producer :as producer]
            [world-sim.consumer :as consumer]
            [world-sim.init :as init]
            [clojure.core.async :refer [poll!]]
            [ui.screen :refer [show-window]]))
(defn clean-world
  []
  (let [world maps/world]
    (dosync (ref-set (get-in world [:gaia :trees :pool :birch :pool]) {})
            (ref-set (get-in world [:gaia :trees :pool :elm :pool]) {})
            (ref-set (get-in world [:gaia :trees :pool :oak :pool]) {})
            (ref-set (get-in world [:gaia :trees :pool :birch :locks]) {})
            (ref-set (get-in world [:gaia :trees :pool :elm :locks]) {})
            (ref-set (get-in world [:gaia :trees :pool :oak :locks]) {})
            (ref-set (get-in world [:enviroment :landmasses :pool]) {})
            (ref-set (get-in world [:physics :time]) 0))
    (while (poll! (:events world))))
  (println "world cleaned!"))

(defn simulator
  [world]
  (producer/start-job world)
  world)

(defn init-world
  [world]
  (init/ini-start world))

(defn start-consumers
  []
  #_#_#_(consumer/start-job maps/world)
  (consumer/start-job maps/world)
  (consumer/start-job maps/world)
  (consumer/start-job maps/world))

(defn run
  []
  (future
    (let [world maps/world]
      (clean-world)                                         ;;just for repl when run multiple times
      (dosync (ref-set (get-in world [:system :main-running?]) true))
      (start-consumers))
    (loop [world (init-world maps/world)
           i (-> world
                 :physics
                 :time
                 deref)]
      (if-not (#{500} i)
        (do
          (Thread/sleep 1)
          (dosync (ref-set (get-in world [:physics :time]) i))
          (recur (simulator world) (inc i)))
        (do
          (println "done")
          (Thread/sleep 200)
          (dosync (ref-set (get-in world [:system :main-running?]) false))
          (clojure.core.async/>!! (:events world) "stopped")
          (clojure.core.async/>!! (:events world) "stopped")
          (clojure.core.async/>!! (:events world) "stopped")
          (clojure.core.async/>!! (:events world) "stopped")
          nil)))))

(defn continue
  [time]
  (future
    (dosync (ref-set (get-in maps/world [:system :main-running?]) true))
    (start-consumers)
    (let [target-time (+ @(get-in maps/world [:physics :time]) time)]
      (loop [world maps/world
             i (-> world
                   :physics
                   :time
                   deref)]
        (if-not (#{target-time} i)
          (do
            (dosync (ref-set (get-in world [:physics :time]) i))
            (recur (simulator world) (inc i)))
          (do
            (println "done")
            (Thread/sleep 200)
            (dosync (ref-set (get-in world [:system :main-running?]) false))
            (clojure.core.async/>!! (:events world) "stopped")
            (clojure.core.async/>!! (:events world) "stopped")
            (clojure.core.async/>!! (:events world) "stopped")
            (clojure.core.async/>!! (:events world) "stopped")
            nil))))))
