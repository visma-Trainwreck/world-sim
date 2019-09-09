(ns world-sim.simulate
  (:require [world-sim.mock-maps :as maps :refer [world]]
            [world-sim.producer :as producer]
            [world-sim.consumer :as consumer]
            [world-sim.init :as init]
            [clojure.core.async :refer [poll!]]
            [ui.screen :refer [show-window]]))

(defn clean-world
  []
  (let [world maps/world]
    (swap! (get-in world [:gaia :trees :pool :birch :pool]) (fn [_] {}))
    (swap! (get-in world [:gaia :trees :pool :elm :pool]) (fn [_] {}))
    (swap! (get-in world [:gaia :trees :pool :oak :pool]) (fn [_] {}))
    (swap! (get-in world [:gaia :trees :pool :birch :locks]) (fn [_] {}))
    (swap! (get-in world [:gaia :trees :pool :elm :locks]) (fn [_] {}))
    (swap! (get-in world [:gaia :trees :pool :oak :locks]) (fn [_] {}))
    (swap! (get-in world [:living-entities :animal :pool :horse :locks]) (fn [_] {}))
    (swap! (get-in world [:living-entities :animal :pool :horse :pool]) (fn [_] {}))
    (swap! (get-in world [:enviroment :landmasses :pool]) (fn [_] {}))
    (swap! (get-in world [:physics :time]) (fn [_] 0))
    (while (poll! (:events world))))
  (println "world cleaned!"))

(defn start-producers
  [world]
  (producer/producer-start-job world 1))

(defn init-world
  [world]
  (init/ini-start world)
  nil)

(defn start-consumers
  [world]
  (consumer/start-job world))

(defn run
  []
  (future
    (clean-world)                                           ;;just for repl when run multiple times
    (swap! (get-in world [:system :main-running?]) (fn [_] true))
    (start-consumers world)
    (start-producers world)
    (init-world world)
    (loop [i (-> world
                 :physics
                 :time
                 deref)]
      (if-not (#{5} i)
        (do
          (Thread/sleep 50)
          (swap! (get-in world [:physics :time]) (fn [_] i))
          (producer/put-event-jacks world)
          (recur (inc i)))
        (do
          (println "done")
          (Thread/sleep 200)
          (swap! (get-in world [:system :main-running?]) (fn [_] false))
          (clojure.core.async/>!! (:events world) "stopped")
          (clojure.core.async/>!! (:events world) "stopped")
          (clojure.core.async/>!! (:events world) "stopped")
          (clojure.core.async/>!! (:events world) "stopped")
          (clojure.core.async/>!! (:event-gather-starter world) "stopped")
          (clojure.core.async/>!! (:event-gather-starter world) "stopped")
          (clojure.core.async/>!! (:event-gather-starter world) "stopped")
          (clojure.core.async/>!! (:event-gather-starter world) "stopped")
          nil)))))

(defn continue
  [time]
  (future
    (swap! (get-in maps/world [:system :main-running?]) (fn [_] true))
    (start-consumers maps/world)
    (start-producers maps/world)
    (let [target-time (+ @(get-in maps/world [:physics :time]) time)]
      (loop [world maps/world
             i (-> world
                   :physics
                   :time
                   deref)]
        (if-not (#{target-time} i)
          (do
            (swap! (get-in world [:physics :time]) (fn [_] i))
            (recur (producer/put-event-jacks world) (inc i)))
          (do
            (println "done")
            (Thread/sleep 200)
            (swap! (get-in world [:system :main-running?]) (fn [_] false))
            (clojure.core.async/>!! (:events world) "stopped")
            (clojure.core.async/>!! (:events world) "stopped")
            (clojure.core.async/>!! (:events world) "stopped")
            (clojure.core.async/>!! (:events world) "stopped")
            nil))))))
