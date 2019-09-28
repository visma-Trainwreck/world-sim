(ns fsm.state
  (:require [fsm.utils :as u]
            [tilakone.core :as tk :refer [_]]))




(defn apply-signal
  "Accepts a FSM (Finite State Machine) and a signal, applies the signal to the FSM
  and returns (possibly) updated FSM."
  [fsm signal]
  (let [from-state (-> fsm ::state)
        transition (-> fsm (u/get-transition signal))
        to-state (-> transition ::to (or from-state))]
    (-> fsm
        (u/apply-actions signal transition)
        (assoc ::state to-state))))


(defn apply-guards
  "Accepts a FSM and a signal, resolves all transitions that are possible with given
  signal, returns seq of tuples of `[transition guard-results]`, where `guard-results` is
  a seq of results reported by guards, each result is a map with `:tilakone.core/allow?`
  (boolean indicating guard verdict), `:tilakone.core/guard` (the guard data from fsm), and
  `:tilakone.core/result` (the return value of guard, or an exception)."
  [fsm signal]
  (->> (u/get-transitions fsm signal)
       (map (fn [transition]
              [transition (u/apply-guards fsm signal transition)]))))


(defn transfers-to
  "Accepts a FSM and a signal, returns the name of the state the signal would
  transfer the FSM if applied. Returns `nil` if signal is not allowed."
  [fsm signal]
  (->> (apply-guards fsm signal)
       (u/find-first (fn [[_ guard-results]]
                       (every? ::allow? guard-results)))
       first
       ::to))


(defn wake-up
  [ctx]
  (assoc-in ctx [::tk/process :stats :current-path] "stand-up"))
(defn lie-down
  [ctx]
  (assoc-in ctx [::tk/process :stats :current-path] "lie down"))

(def test-me
  [{::tk/name        :choice-handle
    ::tk/transitions [{::tk/to      :sleep
                       ::tk/on      _
                       ::tk/actions []}
                      {::tk/to :eat
                       ::tk/on ""}
                      {::tk/to :travel
                       ::tk/on "t"}
                      {::tk/to :play
                       ::tk/on ""}]}
   {::tk/name        :sleep
    ::tk/transitions [{::tk/to      :choice-handle
                       ::tk/on      ""
                       ::tk/actions []}]
    ::tk/enter       {::tk/guards  [(fn [{::tk/keys [signal] :as ctx}]
                                      (if (= signal "s")
                                        (do (println "not allowed!")
                                            true)
                                        false))]
                      ::tk/actions [(fn [m] (println "zzzzzzzzzzzzz")
                                      (lie-down m))]}
    ::tk/leave       {::tk/actions [(fn [m] (println "waking up!")
                                      (wake-up m))]}}
   {::tk/name        :eat
    ::tk/enter        {::tk/actions [(fn [fsm] (def the-fsm fsm) fsm)]}
    ::tk/transitions [{::tk/to :choice-handle
                       ::tk/on ""}]}
   {::tk/name        :travel
    ::tk/stay        {::tk/guards  []
                      ::tk/actions [(fn [m] (println "we stayed!") m)]}

    ::tk/leave       {::tk/guards  [(fn [_] false)]
                      ::tk/actions [(fn [m] (println "nope !") m)]}

    ::tk/transitions [{::tk/to :choice-handle
                       ::tk/on ""}
                      {::tk/on ""}]}

   {::tk/name        :play
    ::tk/transitions [{::tk/to :choice-handle
                       ::tk/on ""}]}
   {::tk/name        :test
    ::tk/stay        {::tk/guards  []
                      ::tk/actions [(fn [_] (println "we stayed!"))]}
    ::tk/transitions [{::tk/on ""}]}])

(def ini-state
  {::tk/states  test-me
   ::tk/action! (fn [{::tk/keys [action] :as fsm}] (action fsm))
   ::tk/guard?  (fn [{::tk/keys [signal guard] :as fsm}] (println signal) (guard fsm))
   ::tk/state   :choice-handle
   :stats       {:count        0
                 :current-path nil
                 :tired?       false}})





(comment

  (tilakone.util/try-guard)






  (def light-machine
    {::tk/states [{::tk/name        "green"
                   ::tk/transitions [{::tk/on "TIMER"
                                      ::tk/to "yellow"}]}
                  {::tk/name        "yellow"
                   ::tk/transitions [{::tk/on "TIMER"
                                      ::tk/to "red"}]}
                  {::tk/name        "red"
                   ::tk/transitions [{::tk/on "TIMER"
                                      ::tk/to "green"}]}]
     ::tk/state  "green"})





  (def FSM
    {::state   Any                                          ;                                     Current state
     ::states  [{::name        "first"                      ;                     State name (can be string, keyword, symbol, any clojure value)
                 ::desc        "test"                       ;                     Optional state description
                 ::transitions [{::name    "out"            ;         Transition name
                                 ::desc    "get out"        ;         Transition description
                                 ::to      "second"         ;         Name of the next state
                                 ::on      :second          ;     Data for match?, does the signal match this transition?
                                 ::guards  []               ;     Data for guard?, is this transition allowed?
                                 ::actions []}]             ;  Actions to be performed on this transition
                 ; Guards and actions used when state is transferred to this stateP
                 ::enter       {::guards  []
                                ::actions [(fn [_] (println "weee"))]}
                 ; Guards and actions used when state is transferred from this state:
                 ::leave       {::guards  []
                                ::actions [(fn [_] (println "bai bai"))]}
                 ; Guards and actions used when state transfer is not made:
                 ::stay        {::guards  []
                                ::actions [(fn [_] (println "nope !"))]}}]
     ::match?  (fn [signal on] ... true/false)              ;   Signal matching predicate
     ::guard?  (fn [{:tilakone.core/keys [signal guard] :as fsm}] ... true/false) ;   Guard function
     ::action! (fn [{:tilakone.core/keys [signal action] :as fsm}] ... fsm)}) ;       Action function

  (def count-ab
    [{::tk/name        :start
      ::tk/transitions [{::tk/on \a, ::tk/to :found-a}]}
     {::tk/name        :found-a
      ::tk/transitions [{::tk/on \a ::tk/to :start}
                        {::tk/on \b ::tk/to :start ::tk/actions [(fn [m] (assoc-in m [::tk/process :count] (+ 1 (:count (::tk/process m)))))]}]}])


  (def count-ab-process
    {::tk/states  count-ab
     ::tk/action! (fn [{::tk/keys [action] :as fsm}] (action fsm))
     ::tk/state   :start
     :count       3})


  (-> count-ab-process
      (tk/apply-signal \a))

  (-> count-ab-process
      (tk/apply-signal \a)
      (tk/apply-signal \b))
  )