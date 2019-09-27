(ns path-finder.tools
  #_(:require [world-sim.mock-maps :refer [world]]))

(defn range-finder
  [start end]
  (let [[xs ys] start
        [xe ye] end
        range-x (if (> xs xe) [xe (+ 1 xs)] [xs (+ 1 xe)])
        range-y (if (> ys ye) [ye (+ 1 ys)] [ys (+ 1 ye)])]
    [range-x range-y]))

#_(defn tester
  [start end]
  (let [tile-map-ref (get-in world [:enviroment :landmasses :pool])
        [xs ys] start
        [xe ye] end
        [range-x range-y] (range-finder start end)
        [xstart xend] range-x
        [ystart yend] range-y]
    (vec (for [x (range xstart xend)]
           (vec (for [y (range ystart yend)
                      :let [tile (get @tile-map-ref [x y])]
                      :when tile]
                  (if (:taken? tile)
                    1
                    0)))))))

(defn- tile-find-surroundings
  [tile-id already-searched search-range]
  (for [y (range (- (second tile-id) search-range) (+ (second tile-id) (+ search-range 1)))
        x (range (- (first tile-id) search-range) (+ (first tile-id) (+ search-range 1)))
        :when (not (contains? already-searched [x y]))]
    [x y]))

(defn find-nearest
  [start-pos search-key world]
  (println "STATS: " start-pos)
  (println "STATS: " search-key)
  (try (let [tile-pool (get-in world [:enviroment :landmasses :pool])]
         (loop [search-range 1
                arr #{start-pos}]
           (let [checked-group (tile-find-surroundings start-pos arr search-range)
                 neighbours (->> (for [tile-id checked-group]
                                   (let [tile (get @tile-pool tile-id)]
                                     (when (and tile (> (search-key tile) 5))
                                       tile-id)))
                                 (remove nil?))]
             (if (and
                   (do (def tester neighbours)
                       true)
                   (empty? neighbours)
                   (< search-range 2000000000))
               (recur (+ search-range 1) (conj arr checked-group))
               (do
                 (println "NEIGHBOURS: " neighbours)
                 neighbours)))))
       (catch Exception e
         (println "error?")
         e)))