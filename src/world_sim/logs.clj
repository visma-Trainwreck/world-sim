(ns world-sim.logs)




(defn start-logging
  [world]
  (let [running? (get-in world [:system :main-running?])
        events-ref @(get-in world [:system :events-done])]
    (while @running?
      (println @events-ref " events was consumed!")
      (send events-ref (fn [_] 0))
      (Thread/sleep 1000))))