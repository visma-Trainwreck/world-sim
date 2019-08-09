(ns world-sim.mock-maps
  (:require [world-sim.actions :as actions]
            [clojure.core.async :refer [chan]]))


(def birch
  {:id       nil :name "birch" :size 0 :last-check nil :last-birth 0 :health 1 :death-date nil :births-amount 0
   :location {:x nil :y nil :tile-id nil}})

(def oak
  {:id       nil :name "oak" :size 0 :last-check nil :last-birth 0 :health 1 :death-date nil :births-amount 0
   :location {:x nil :y nil :tile-id nil}})

(def elm
  {:id       nil :name "elm" :size 0 :last-check nil :last-birth 0 :health 1 :death-date nil :births-amount 0
   :location {:x nil :y nil :tile-id nil}})

(def tulip
  {:id       nil :name "tulip" :size 0 :last-check nil :last-birth 0 :health 1 :death-date nil :births-amount 0
   :location {:x nil :y nil :tile-id nil}})

(def horse
  {:id       nil :name "horse" :size 0 :last-check nil :last-birth 0 :health 1 :death-date nil :births-amount 0
   :location {:x nil :y nil :tile-id nil}})

(def tile {:id nil :x nil :y nil :taken? false})

(def world
  {:gaia            {:trees  {:actions [actions/entity-grow actions/entity-birth actions/entity-death actions/entity-remove]
                              :pool    {:birch {:pool           (ref {})
                                                :locks          (ref {})
                                                :path-key       :birch-tree
                                                :class-name     :birch
                                                :base-rot-time  1
                                                :base-growth    1
                                                :base-size      10
                                                :base-lifespan  100
                                                :birth-min      3
                                                :birth-max      1000
                                                :birth-cooldown 10
                                                :newborn        birch}
                                        :oak   {:pool           (ref {})
                                                :locks          (ref {})
                                                :path-key       :oak-tree
                                                :class-name     :oak
                                                :base-rot-time  15
                                                :base-growth    1
                                                :base-size      10
                                                :base-lifespan  500
                                                :birth-min      5
                                                :birth-max      1000
                                                :birth-cooldown 20
                                                :newborn        oak}
                                        :elm   {:pool           (ref {})
                                                :locks          (ref {})
                                                :path-key       :elm-tree
                                                :class-name     :elm
                                                :base-rot-time  3
                                                :base-growth    1
                                                :base-size      10
                                                :base-lifespan  200
                                                :birth-min      1
                                                :birth-max      1000
                                                :birth-cooldown 13
                                                :newborn        elm}}
                              }
                     :plants {:actions []
                              :pool    {:tulips {:pool           (ref {})
                                                 :locks          (ref {})
                                                 :path-key       :tulip-plant
                                                 :class-name     :tulips
                                                 :base-growth    1
                                                 :base-size      10
                                                 :base-lifespan  40
                                                 :birth-min      1
                                                 :birth-max      1000
                                                 :birth-cooldown 1
                                                 :newborn        birch}}}}
   :physics         {:time (ref 0)}
   :enviroment      {:landmasses {:class-name :landmasses
                                  :tile       tile
                                  :pool       (ref {})}
                     :mountain   {:pool []}
                     :lakes      {:pool []}
                     :ocean      nil}
   :living-entities {:human  {:pool          []
                              :base-growth   1
                              :base-lifespan 1}
                     :animal {:actions [actions/entity-grow actions/entity-birth actions/entity-death]
                              :pool {:horse {:pool         []
                                             :locks        []
                                             :class-name     :horse
                                             :base-growth    1
                                             :base-size      10
                                             :base-lifespan  40
                                             :birth-min      1
                                             :birth-max      1000
                                             :birth-cooldown 1
                                             :newborn      horse
                                             :current-goal nil}}}}
   :events          (chan 1024)
   :system          {:main-running? (ref false)
                     :events-done   (agent 0)}})





