(ns world-sim.tools
  (:require [clojure.core.async :refer [<!! <! >!!]])
  (:import (java.util UUID)))

(defn print-exception
  [world e]
  (if (:exceptions (:system world))
    (println e)))

(declare input)

#_(defn fsm-fn-runner
  [world action {:keys [entity-id entity-class]}]
  (loop [events-list (:events action)
         status true]
    (if (and status (first events-list))
      (recur
        (rest events-list)
        (input world entity-class entity-id (first events-list) status)))))

(defn id-creater-entity
  [entity]
  (conj entity {:id (keyword (.toString (UUID/randomUUID)))}))

(defn id-creater
  []
  (.toString (UUID/randomUUID)))

(defn events-queue-pop
  [world]
  (let [channel (get-in world [:events])
        event (<!! channel)]
    event))

(defn events-queue-add
  [world event]
  (let [channel (get-in world [:events])]
    (>!! channel event)))

(defn conditional-map-func
  [entity-pool entity condition]
  (cond
    (= condition :add) (conj @entity-pool {(:id entity) entity})
    (= condition :remove) (dissoc @entity-pool entity)
    :else  (println "condition was not found for entity " entity " condition: " condition)))

(defn input [world entity-class entity f res-before]
  (let [result-map (f world entity-class entity res-before) ;;should return a map in future yea? yea!
        entity-new (:entity-new result-map)
        entity-pool (:pool (:entity-class result-map))
        entity-pool-new (conditional-map-func entity-pool entity-new (:opt result-map))]
    (if entity-new
      (swap! entity-pool (fn [_] entity-pool-new))
      nil)
    (:func-return result-map)))

#_(defn input
  [world entity-class entity f res-before]
  (let [entity-pool (:pool entity-class)]
    (swap! entity-pool (fn [_] (let [result-map (f world entity-class entity res-before) ;;should return a map in future yea? yea!
                                     entity-new (:entity-new result-map)
                                     entity-pool (:pool (:entity-class result-map))
                                     entity-pool-new (conditional-map-func entity-pool entity-new (:opt result-map))]
                                 entity-pool-new)))))

#_(defn input
  "This function is supposed to only change atoms when in the lambda function in a swap.
  Unfortunately we also expect the function to return something that was derived within the lambda
  therefore we set an atom in the lambda to be able to make the function return the derived result.
  ... halp <(^.^<)"
  [world entity-class entity f res-before]
  (let [return-val (atom nil)]
    (swap! (:pool entity-class)
           (fn [ori] (let [result-map (f world entity-class entity res-before) ;;should return a map in future yea? yea!
                           entity-new (:entity-new result-map)
                           entity-pool (:pool (:entity-class result-map))
                           entity-pool-new (conditional-map-func entity-pool entity-new (:opt result-map))]
                       (do
                         (swap! return-val (fn [_] result-map))
                         (if entity-new
                           entity-pool-new
                           ori)))))
    (:func-return @return-val)))

#_(defn tester
  [world entity-class entity f res-before]
  (loop [return nil]
    (if (nil? return)
      (swap! (:pool entity-class)
             (fn [ori] (let [result-map (f world entity-class entity res-before) ;;should return a map in future yea? yea!
                             entity-new (:entity-new result-map)
                             entity-pool (:pool (:entity-class result-map))
                             entity-pool-new (conditional-map-func entity-pool entity-new (:opt result-map))]
                         (do
                           (if entity-new
                             entity-pool-new
                             ori))))))))

(defn locate-where
  "Performs a breadth-first recursive search in the nested data structure `m`,
  returning the first nested collection for which `pred` returns true for any element."
  ([path pred m]
   (when (map? m)
     (if (pred m)
       path
       (->> m
            (map (fn [[k v]]
                   (locate-where
                     (conj path k)
                     pred
                     v)))
            (some identity)))))
  ([pred m]
   (locate-where [] pred m)))

(defn now
  [world]
  (-> world
      :physics
      :time
      deref))

(defn lock-set
  [ref-locks id actions]
  (swap! ref-locks (fn [_] (let [locks-map (deref ref-locks)
                                 the-lock (id locks-map)
                                 thelock-updated (if (nil? the-lock)
                                                   {:id id :locked-actions {actions true}}
                                                   {:id             id
                                                    :locked-actions (conj (:locked-actions the-lock) {actions true})})
                                 locks-map-updated (assoc-in locks-map [id] thelock-updated)]
                             locks-map-updated))))

(defn check-lock
  [lock-map entity-id lock-name]
  (let [lock (-> lock-map
                 deref
                 entity-id)]
    (if-not (nil? lock)
      (-> lock
          :locked-actions
          lock-name
          not)
      true)))

(defn release-lock
  [ref-locks id action-name]
  (swap! ref-locks (fn [_] (let [updated-locks (assoc-in @ref-locks [id :locked-actions action-name] false)]
                             updated-locks))))

(defn get-neighbours
  [[x y] deltas]
  (mapv
    (fn [[dx dy]] [(+ x dx) (+ y dy)])
    deltas))

(defmulti sight-pattern :name)
(defmethod sight-pattern :default
  [_]
  [[-1 -1] [-1 0] [-1 1]
   [ 0 -1]        [ 0 1]
   [ 1 -1] [1  0] [ 1 1]])
#_(defmethod sight-pattern "cow"
  [cow]
  (let [direction (:current-direction cow)]
    direction))                                                       ;; cow is blind

(defn tile-get-neighbours
  [tile-ref tile-id]
  (remove nil?
          (get-neighbours tile-id (sight-pattern (get @tile-ref tile-id)))
          #_(mapv (partial get @tile-ref)
                (get-neighbours tile-id (sight-pattern (get @tile-ref tile-id))))))

#_(defn update-tile
    [world tile]
    (let [tiles-ref (get-in world [:enviroment :landmasses :pool])
          tile-updated (conj tile {:taken? true})]
      (dosync (ref-set tiles-ref (conj @tiles-ref {(:id tile) tile-updated})))))

(defn freetile-locate
  [tile-ref tiles-id]
  (let [tiles (map (fn [id] (get @tile-ref id)) tiles-id)
        possible-tiles (filter (fn [tile] (if-not (:taken? tile)
                                            tile)) tiles)]
    (if (= (count possible-tiles) 0)
      nil
      possible-tiles)))

(defn update-tile
  [entity]
  (let [tile-updated (conj entity {:taken? true})]
    (if entity
      tile-updated
      nil)))

(defn freetile-locate-qeueue
  [world entity-class entity]
  (let [entity-actual (get @(:pool entity-class) entity)
        tile-id (get-in entity-actual [:location :tile-id])
        tile-ref (get-in world [:enviroment :landmasses :pool])
        nighbours (tile-get-neighbours tile-ref tile-id)
        tiles (freetile-locate tile-ref nighbours)]
    (if tiles
      (rand-nth tiles)
      tiles)))

(defn find-nearest
  [world entity item]
  (let [tile-id (get-in entity [:location :tile-id])
        tile-map @(get-in world [:enviroment :landmasses :pool])
        tile (tile-id @(get-in world [:enviroment :landmasses :pool]))]
    (for [i 0]
      (print "fail"))))

(defn return-as-map
  [world entity-class entity-id]
  {:world        world
   :entity-class entity-class
   :entity-id    entity-id})