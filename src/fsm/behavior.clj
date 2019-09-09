(ns fsm.behavior
  (:require [fsm.utils :as u]
            [tilakone.core :as tk :refer [_]]))

(def states
  [{::tk/name        :choice-handle
    ::tk/transitions [{::tk/on _
                       ::tk/to :travel}
                      {::tk/on _
                       ::tk/to :sleep}
                      {::tk/on _
                       ::tk/to :eat}]}
   {::tk/name        :travel
    ::tk/transitions [{::tk/on _
                       ::tk/to :choice-handle}]}
   {::tk/name        :sleep
    ::tk/transitions [{::tk/on _
                       ::tk/to :choice-handle}]}
   {::tk/name        :eat
    ::tk/transitions [{::tk/on _
                       ::tk/to :choice-handle}]}])

(def ini-state
  {::tk/states   states
   ::tk/action! (fn [{::tk/keys [action] :as fsm}] (action fsm))
   ::tk/guard?  (fn [{::tk/keys [signal guard] :as fsm}] (guard fsm))
   ::tk/state   :choice-handle
   :stats       {
                 :current-path nil}})