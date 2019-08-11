(ns world-sim.tools
  (:require [clojure.core.async :refer [<!! <! >!!]])
  (:import (java.util UUID)))

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
    :else (println condition)))

(defn input [world entity-class entity f res-before]
  (let [result-map (f world entity-class entity res-before) ;;should return a map in future yea? yea!
        entity-new (:entity-new result-map)
        entity-pool (:pool (:entity-class result-map))
        entity-pool-new (conditional-map-func entity-pool entity-new (:opt result-map))]
    (if entity-new
      (swap! entity-pool (fn [_] entity-pool-new) )
      nil)
    (:func-return result-map)))

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

(defn tile-get-neighbours
  [tile-ref tile-id]
  (for [y (range (- (second tile-id) 1) (+ (second tile-id) 2))
        x (range (- (first tile-id) 1) (+ (first tile-id) 2))
        :when (and (get @tile-ref [x y])
                   (not (= [x y] tile-id)))]
    [x y]))

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

#_(defn xxxxxx
  [world tiles-ref tile-id]
  ;; an atom in a let binding :S  shucks...
  ;; we are doing something wrong !
  (let [event-return (atom nil)]
    (dosync (ref-set tiles-ref
                     (let [neighbours (tile-get-neighbours tiles-ref tile-id)
                           tile (rand-nth (freetile-locate tiles-ref neighbours))]
                       (if tile
                         (do
                           (swap! event-return (fn [_] tile))
                           (conj @tiles-ref {(:id tile) (conj tile {:taken? true})}))
                         @tiles-ref))))
    @event-return))