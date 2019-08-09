(ns world-sim.init
  (:require [world-sim.tools :as tools]
            [world-sim.mock-maps :as maps]
            [clojure.core.async :as async])
  (:import (clojure.lang PersistentQueue)))

(defn create-trees
  [world]
  (println "create trees")
  (let [tiles-pool (get-in world [:enviroment :landmasses :pool])
        tile-key-birch (rand-nth (keys (deref tiles-pool)))  ;; a little expensive ??
        tile-key-oak (rand-nth (keys (deref tiles-pool)))
        entity-pool-birch (get-in world [:gaia :trees :pool :birch :pool])
        entity-pool-oak (get-in world [:gaia :trees :pool :oak :pool])
        tile-birch (conj (get (deref tiles-pool) tile-key-birch) {:taken? true})
        tile-oak (conj (get (deref tiles-pool) tile-key-oak) {:taken? true})
        tree-id-birch (keyword (tools/id-creater))
        tree-id-oak (keyword (tools/id-creater))
        tree-birch (conj maps/birch {:id tree-id-birch
                               :born (tools/now world)
                               :location {:x (:x tile-birch) :y (:y tile-birch) :tile-id (:id tile-birch)}
                               :last-birth (tools/now world)})
        tree-oak (conj maps/birch {:id tree-id-oak
                                   :born (tools/now world)
                                   :location {:x (:x tile-oak) :y (:y tile-oak) :tile-id (:id tile-oak)}
                                   :last-birth (tools/now world)})
        tiles-pool-updated (conj (deref tiles-pool) {tile-key-birch tile-birch
                                                     tile-key-oak tile-oak})
        entity-pool-birch-updated (conj (deref entity-pool-birch) {tree-id-birch tree-birch})
        entity-pool-oak-updated (conj (deref entity-pool-birch) {tree-id-oak tree-oak})]
    (dosync
      (ref-set tiles-pool tiles-pool-updated)
      (ref-set entity-pool-birch entity-pool-birch-updated)
      (ref-set entity-pool-oak entity-pool-oak-updated))
    world))

(defn test-tree-populate
  [world]
  (let [tree-pool (get-in world [:gaia :trees :pool])]
    (doseq [[key tree-class] tree-pool
            :let [tiles-pool (get-in world [:enviroment :landmasses :pool])
                  tile-key (rand-nth (keys (deref tiles-pool)))
                  entity-pool (:pool tree-class)
                  tile (conj (get (deref tiles-pool) tile-key) {:taken? true})
                  tree-id (keyword (tools/id-creater))
                  tree (conj (:newborn tree-class) {:id tree-id
                                         :born (tools/now world)
                                         :location {:x (:x tile) :y (:y tile) :tile-id (:id tile)}
                                         :last-birth (tools/now world)})
                  tiles-pool-updated (conj (deref tiles-pool) {tile-key tile})
                  entity-pool-updated (conj (deref entity-pool) {tree-id tree})]]
      (dosync
        (ref-set tiles-pool tiles-pool-updated)
        (ref-set entity-pool entity-pool-updated))))
  world)

#_(defn create-trees
  [world]
  (println "create trees")
  (let [tiles-pool (get-in world [:enviroment :landmasses :pool])
        entity-pool (get-in world [:gaia :trees :pool :birch :pool])
        tile-key (rand-nth (keys (deref tiles-pool)))       ;; a little expensive ??
        tile (conj (get (deref tiles-pool) tile-key) {:taken? true})
        tree-id (keyword (tools/id-creater))
        tree (conj maps/birch {:id tree-id
                                 :born (tools/now world)
                                 :location {:x (:x tile) :y (:y tile) :tile-id (:id tile)}
                                 :last-birth (tools/now world)})
        tiles-pool-updated (conj (deref tiles-pool) {tile-key tile})
        entity-pool-updated (conj (deref entity-pool) {tree-id tree})]
    (dosync
      (ref-set tiles-pool tiles-pool-updated)
      (ref-set entity-pool entity-pool-updated))
    world))

(def cpu-count (atom 0))
(def map-agent (agent {}))
#_(def amount-since-last (atom 0))

(defn create-tile-map
  [world]
  (println "creating tilemap")
  #_(future (amout-job))
  (dosync (ref-set (get-in world [:enviroment :landmasses :pool]) {}))
  (let [tile (get-in world [:enviroment :landmasses :tile])]
    (doseq [i (range 100)
            j (range 100)]
      #_(while (> @cpu-count 16))
      (do
        #_(swap! cpu-count #(+ % 1))
        (let [id [i j]
              new-tile (conj tile {:id id :x i :y j})]
          (send map-agent (fn [old-map] (conj old-map {(:id new-tile) new-tile}))))
        #_(swap! cpu-count #(- % 1))
        #_(swap! amount-since-last inc))))
  #_(if (> @cpu-count 1)
    (do (Thread/sleep 5000)
        (println "waiting for threads " @cpu-count)))
  (Thread/sleep 2000)                                       ;; making sure the agent proccessed all its messages
  (dosync (ref-set (get-in world [:enviroment :landmasses :pool]) @map-agent))
  (swap! cpu-count (fn [_] 0))
  world)

(defn find-tile
  [ordered-map x y]
  (:id (first (ordered-map [x y]))))

(defn mini-fill
  [key tile ordered-map tile-map]
  (let [x (:x tile)
        y (:y tile)
        updated-tile (assoc-in tile [:neighbour-tile-ids] {:north     (find-tile ordered-map x (+ y 1))
                                                           :south     (find-tile ordered-map x (- y 1))
                                                           :east      (find-tile ordered-map (+ x 1) y)
                                                           :west      (find-tile ordered-map (- x 1) y)
                                                           :northwest (find-tile ordered-map (- x 1) (+ y 1))
                                                           :northeast (find-tile ordered-map (+ x 1) (+ y 1))
                                                           :southwest (find-tile ordered-map (- x 1) (- y 1))
                                                           :southeast (find-tile ordered-map (+ x 1) (- y 1))})]
    (send map-agent (fn [old-map]
                      (conj old-map {key updated-tile})))))

(defn neighbours-fill
  [world]
  (println "finding neightbours!")
  (let [ordered-map (group-by (juxt :x :y) (vals (deref map-agent)))]
    (doseq [[key tile] (deref map-agent)]
      (future (mini-fill key tile ordered-map map-agent))))
  (Thread/sleep 5000)
  (dosync (ref-set (get-in world [:enviroment :landmasses :pool]) @map-agent))
  #_(send map-agent (fn [_] {}))
  world)

(defn ini-start
  [world]
  (let [new-world (-> world
                      create-tile-map
                      #_neighbours-fill
                      test-tree-populate)]
    (println "world inititated")
    new-world))