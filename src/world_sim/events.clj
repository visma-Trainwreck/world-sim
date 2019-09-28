(ns world-sim.events
  (:require [world-sim.tools :as tools]))

(defn entity-has-born
  [world entity-class entity-id _]
  (try (let [entity (get @(:pool entity-class) entity-id)
             entity-new (conj entity {:last-birth    (tools/now world)
                                      :births-amount (+ 1 (:births-amount (entity-id @(:pool entity-class))))})]
         {:entity-class entity-class
          :entity-new   entity-new
          :opt          :add
          :func-return  entity-new})
       (catch Exception e
         (if (:exceptions (:system world))
           (println e)))))

(defn set-entity-born
  [entity world]
  (let [time (tools/now world)]
    (-> entity
        (conj {:born time}))))

(defn find-tile-update
  [world entity-class entity _]
  (let [tile-new (-> (tools/freetile-locate-qeueue world entity-class entity)
                        tools/update-tile)
        tile-class (get-in world [:enviroment :landmasses])]
    {:entity-class tile-class
     :entity-new tile-new
     :opt :add
     :func-return tile-new}))

(defn create-new-entity
  [world entity-class entity-id tile]
  (let [entity-new (:newborn entity-class)
        newborn (-> entity-new
                    (set-entity-born world)
                    (tools/id-creater-entity)
                    (conj {:location {:tile-id (:id tile) :x (:x tile) :y (:y tile)}}))]
    {:entity-class entity-class
     :entity-new newborn
     :opt :add
     :func-return newborn}))

(defn entity-die
  [world entity-class entity-id _]
  (let [entity  (get @(:pool entity-class) entity-id)
        entity-new (conj entity {:death-date (tools/now world)})]
    {:entity-class entity-class
     :entity-new entity-new
     :opt :add
     :func-return entity-new}))

(defn tile-make-free
  [world entity-class entity-id tile-id]
  (let [tile-class (get-in world [:enviroment :landmasses])
        entity-tile-id (:tile-id (:location (entity-id @(:pool entity-class))))
        tile (get @(:pool tile-class) entity-tile-id)
        tile-updated (conj tile {:taken? false})]
    {:entity-class tile-class
     :entity-new tile-updated
     :opt :add
     :func-return tile-updated}))

(defn entity-grow
  [world entity-class entity-id _]
  (let [entity (entity-id @(:pool entity-class))
        size {:size (+ (:size entity)
                             (:base-growth entity-class))}
        entity-new (conj entity size)]
    {:entity-class entity-class
     :entity-new entity-new
     :opt :add
     :func-return entity-new}))

(defn entity-relocate
  [world entity-class entity-id _]
  (let [entity (entity-id @(:pool entity-class))
        current-loc (:location entity)
        current-dir (get-in entity [:plan :current-direction])
        updated-loc (conj current-loc {:x (+ (first current-dir) (:x current-loc))
                                       :y (+ (second current-dir) (:y current-loc))})
        entity-new (assoc-in entity [:location] updated-loc)]
    {:entity-class entity-class
     :entity-new entity-new
     :opt :add
     :func-return entity-new}))

(defn entity-remove
  [_ entity-class entity _]
  [entity-class entity :remove]
  {:entity-class entity-class
   :entity-new entity
   :opt :remove
   :func-return :remove})

