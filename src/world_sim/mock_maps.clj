(ns world-sim.mock-maps
  (:require [world-sim.actions :as actions]
            [world-sim.actions-walkers :as actions-walkers]
            [clojure.core.async :refer [chan]]
            [fsm.behavior :as behavior]))

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

(def cow
  {:id       nil :name "cow" :size 0 :last-check nil :last-birth 0 :health 1 :death-date nil :births-amount 0
   :location {:x nil :y nil :tile-id nil}})

(def horse
  {:id       nil :name "horse" :size 0 :last-check nil :last-birth 0 :health 1 :death-date nil :births-amount 0
   :location {:x nil :y nil :tile-id nil}
   :plan {:current-direction [1 0]
          :current-goal nil}
   :fsm behavior/ini-state})

(def tile {:id nil :x nil :y nil :taken? false})

(def world
  {:gaia            {:trees  {:actions [actions/entity-grow actions/entity-birth actions/entity-death actions/entity-remove]
                              :time-divisor 12
                              :pool    {:birch {:pool           (atom {})
                                                :locks          (atom {})
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
                                        :oak   {:pool           (atom {})
                                                :locks          (atom {})
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
                                        :elm   {:pool           (atom {})
                                                :locks          (atom {})
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
                              :pool    {:tulips {:pool           (atom {})
                                                 :locks          (atom {})
                                                 :path-key       :tulip-plant
                                                 :class-name     :tulips
                                                 :base-growth    1
                                                 :base-size      10
                                                 :base-lifespan  40
                                                 :birth-min      1
                                                 :birth-max      1000
                                                 :birth-cooldown 1
                                                 :newborn        birch}}}}
   :physics         {:time (atom 0)
                     :world-width 100
                     :world-height 100}
   :enviroment      {:landmasses {:class-name :landmasses
                                  :tile       tile
                                  :pool       (atom {})}
                     :mountain   {:pool []}
                     :lakes      {:pool []}
                     :ocean      nil}
   :living-entities {:human  {:pool          []
                              :base-growth   1
                              :base-lifespan 1}
                     :animal {:actions [actions-walkers/entity-fsm]
                              :time-divisor 1
                              :pool {:horse {:pool         (atom {})
                                             :locks        (atom {})
                                             :class-name     :horse
                                             :base-growth    1
                                             :base-size      10
                                             :base-lifespan  40
                                             :birth-min      1
                                             :birth-max      1000
                                             :birth-cooldown 1
                                             :newborn      horse
                                             :plan {:current-direction [1 0]}}
                                     :cow {:pool         (atom {})
                                           :locks        (atom {})
                                           :class-name     :cow
                                           :base-growth    1
                                           :base-size      10
                                           :base-lifespan  40
                                           :birth-min      1
                                           :birth-max      1000
                                           :birth-cooldown 1
                                           :newborn      cow
                                           :plan {:current-direction [1 0]}}}}}
   :events          (chan 1024)
   :event-gather-starter (chan 1024)
   :system          {:main-running? (atom false)
                     :events-done   (agent 0)}})

(def jacks [(get-in world [:gaia :trees])
            (get-in world [:living-entities :animal])])





