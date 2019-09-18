(ns fsm.behavior
  (:require [fsm.utils :as u]
            [tilakone.core :as tk :refer [_]]
            [fsm.fsm-guards :as guard-funcs]
            [fsm.fsm-actions :as action-funcs]
            [world-sim.fsm-events-walker :as events-w]))



(def states
  [{::tk/name        :choice-handle
    ::tk/transitions [{::tk/on _
                       ::tk/to :sleep}
                      {::tk/on _
                       ::tk/to :travel}
                      {::tk/on _
                       ::tk/to :eat}]}
   {::tk/name        :travel
    ::tk/enter       {::tk/guards  [guard-funcs/has-goal]
                      ::tk/actions [fsm.fsm-actions/get-path]}
    ::tk/stay        {::tk/guards  []
                      ::tk/actions [#_travel-path]}
    ::tk/leave       {::tk/guards  guard-funcs/travel-done?
                      ::tk/actions [action-funcs/unset-goal]}
    ::tk/transitions [{::tk/on _
                       ::tk/to :choice-handle}]}

   {::tk/name        :sleep
    ::tk/enter       {::tk/guards  [guard-funcs/go-sleep?]
                      ::tk/actions [(fn [fsm] (println "enter sleep mode!") fsm)]}
    ::tk/stay        {::tk/guards  []
                      ::tk/actions [(fn [fsm]  (action-funcs/sleep fsm))]}
    ::tk/leave       {::tk/guards  [guard-funcs/done-sleep?]
                      ::tk/actions [(fn [fsm] (println "leave sleep mode!" ) fsm)]}
    ::tk/transitions [{::tk/on _
                       ::tk/to :choice-handle}
                      {::tk/on _}]}

   {::tk/name        :eat
    ::tk/enter       {::tk/guards  [guard-funcs/go-eat?]
                      ::tk/actions [action-funcs/eat]}
    ::tk/stay        {::tk/guards  []
                      ::tk/actions [(fn [fsm] (action-funcs/eat fsm))]}
    ::tk/leave       {::tk/guards  [guard-funcs/done-eat?]
                      ::tk/actions []}
    ::tk/transitions [{::tk/on      _
                       ::tk/to      :travel
                       ::tk/name    "travel"
                       ::tk/desc    "travel to food"
                       ::tk/guards  [guard-funcs/can-eat?]
                       ::tk/actions [(fn [fsm] (action-funcs/set-goal fsm :eat))]}
                      {::tk/on _
                       ::tk/to :choice-handle}
                      {::tk/on _}]}])

(def ini-state
  {::tk/states   states
   ::tk/action! (fn [{::tk/keys [action] :as fsm}] (action fsm))
   ::tk/guard?  (fn [{::tk/keys [signal guard] :as fsm}] (guard fsm))
   ::tk/state   :choice-handle
   :stats       {:current-goal nil
                 :current-path nil
                 :events []}})