(ns world-sim.init
  (:require [world-sim.tools :as tools]))

(defn test-tree-populate
  [world]
  (println "placing trees")
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

      (swap! tiles-pool (fn [_] tiles-pool-updated))
      (swap! entity-pool (fn [_] entity-pool-updated))))
  world)

(defn animal-populate
  [world]
  (let [horse-class (get-in world [:living-entities :animal :pool :horse])
        horse-pool (:pool horse-class)
        horse (:newborn horse-class)
        horse-updated (-> horse
                          (conj {:id (keyword (tools/id-creater))})
                          (assoc-in [:location] {:x 0 :y 0 :tile-id [0 0]})
                          (assoc-in [:plan :current-direction] [1 0]))]
    (swap! horse-pool (fn [pool] (conj pool {(:id horse-updated) horse-updated})))
    world))

(def cpu-count (atom 0))
(def map-agent (agent {}))
#_(def amount-since-last (atom 0))

(def grass-tile [45 72])

(defn create-tile-map
  [world]
  (println "creating tilemap")
  #_(future (amout-job))
  (swap! (get-in world [:enviroment :landmasses :pool]) (fn [_] {}))
  (let [tile (get-in world [:enviroment :landmasses :tile])]
    (doseq [i (range (get-in world [:physics :world-width]))
            j (range (get-in world [:physics :world-height]))]
      #_(while (> @cpu-count 16))
      (do
        #_(swap! cpu-count #(+ % 1))
        (let [id [i j]
              new-tile (conj tile {:id id :x i :y j})]
          (if (= grass-tile [i j])
            (send map-agent (fn [old-map] (conj old-map {(:id new-tile) (conj new-tile {:grass 100})})))
            (send map-agent (fn [old-map] (conj old-map {(:id new-tile) new-tile})))))
        #_(swap! cpu-count #(- % 1))
        #_(swap! amount-since-last inc))))
  #_(if (> @cpu-count 1)
    (do (Thread/sleep 5000)
        (println "waiting for threads " @cpu-count)))
  (Thread/sleep 2000)                                       ;; making sure the agent proccessed all its messages
  (swap! (get-in world [:enviroment :landmasses :pool]) (fn [_] @map-agent))
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
  (swap! (get-in world [:enviroment :landmasses :pool]) (fn [_] @map-agent))
  #_(send map-agent (fn [_] {}))
  world)

(defn ini-start
  [world]
  (let [new-world (-> world
                      create-tile-map
                      animal-populate
                      test-tree-populate)]
    (println "world inititated")
    new-world))