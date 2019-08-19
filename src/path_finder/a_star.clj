(ns path-finder.a-star
  (:require [world-sim.mock-maps :as maps :refer [world]]
            [path-finder.tools :as tools]
            [clojure.data.priority-map :refer [priority-map-by]]))

(defn distance
  [start end]

  (+ (Math/abs ^Integer (- (first start) (first end)))
     (Math/abs ^Integer (- (second start) (second end)))))

(defn cost
  [curr start end]
  (let [g (distance start curr)
        h (distance curr end)
        f (+ g h)]
    [f g h]))

(defn edges
  [map width height closed [x y]]
  (for [tx (range (- x 1) (+ x 2))
        ty (range (- y 1) (+ y 2))
        :when (and (>= tx 0)
                   (>= ty 0)
                   (<= tx width)
                   (<= ty height)
                   (not= [x y] [tx ty])
                   (not (:taken? (get map [tx ty])))
                   (not (contains? closed [tx ty])))]
    [tx ty]))

(defn path [end parent closed]
  (reverse
    (loop [path [end parent]
           node (closed parent)]
      (if (nil? node)
        path
        (recur (conj path node) (closed node))))))

#_(use '[clojure.data.priority-map])

(defn search
  ([map start end]
   (let [[sx sy] start
         [ex ey] end
         open (priority-map-by
                (fn [x y]
                  (if (= x y)
                    0
                    (let [[f1 _ h1] x
                          [f2 _ h2] y]
                      (if (= f1 f2)
                        (if (< h1 h2) -1 1)
                        (if (< f1 f2) -1 1)))))
                start (cost start start end))
         closed {}
         width  (get-in world [:physics :world-width]) #_(-> map first count dec)
         height (get-in world [:physics :world-height]) #_(-> map count dec)]
     (when (and (not (:taken? (get map [sx sy])))
                (not (:taken? (get map [ex ey]))))
       (search map width height open closed start end))))

  ([map width height open closed start end]
   (if-let [[coord [_ _ _ parent]] (peek open)]
     (if-not (= coord end)
       (let [closed (assoc closed coord parent)
             edges (edges map width height closed coord)
             open (reduce
                    (fn [open edge]
                      (if (not (contains? open edge))
                        (assoc open edge (conj (cost edge start end) coord))
                        (let [[_ pg] (open edge)
                              [nf ng nh] (cost edge start end)]
                          (if (< ng pg)
                            (assoc open edge (conj [nf ng nh] coord))
                            open))))
                    (pop open) edges)]
         (recur map width height open closed start end))
       (path end parent closed)))))

(defn get-path
  [pos-start pos-goal]
  #_(let [translated-map (tools/tester pos-start pos-goal)]
    (search translated-map pos-start pos-goal))
  (search @(get-in world [:enviroment :landmasses :pool]) pos-start pos-goal))