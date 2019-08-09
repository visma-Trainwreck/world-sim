(ns world-sim.actions
  (:require [world-sim.events :as events]
            [world-sim.tools :as tools]))

(def entity-death
  {:name      :death
   :condition (fn [world entity-class entity]
                (let [entity-age (- (tools/now world) (:born entity))]
                  (and
                    (not (:death-date entity))
                    (> entity-age (:base-lifespan entity-class)))))
   :events    [events/entity-die]})

(def entity-birth
  {:name      :birth
   :condition (fn [world entity-class entity]
                (let [entity-age (- (tools/now world) (:born entity))
                      last-birth (- (tools/now world) (:last-birth entity))
                      birth-cooldown (:birth-cooldown entity-class)
                      birth-min (:birth-min entity-class)
                      birth-max (:birth-max entity-class)]
                  (and
                    (not (:death-date entity))
                    (< birth-cooldown last-birth)
                    (< entity-age birth-max)
                    (> entity-age birth-min))))
   :events    [events/find-tile-update events/create-new-entity events/entity-has-born]})

(def entity-grow
  {:name      :grow
   :condition (fn [world entity-class entity]
                (let [max-size (:base-size entity-class)
                      entity-size (:size entity)]
                  (and
                    (not (:death-date entity))
                    (> max-size entity-size))))
   :events    [events/entity-grow]})

(def entity-remove
  {:name      :remove
   :condition (fn [world entity-class entity]
                (if (:death-date entity)
                  (let [time-dead (- (tools/now world) (:death-date entity))]
                    (> time-dead (:base-rot-time entity-class)))
                  false))
   :events    [events/tile-make-free events/entity-remove]})