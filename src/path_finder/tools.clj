(ns path-finder.tools
  (:require [world-sim.mock-maps :refer [world]]))

(defn range-finder
  [start end]
  (let [[xs ys] start
        [xe ye] end
        range-x (if (> xs xe) [xe (+ 1 xs)] [xs (+ 1 xe)])
        range-y (if (> ys ye) [ye (+ 1 ys)] [ys (+ 1 ye)])]
    [range-x range-y]))

(defn tester
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