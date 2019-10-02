(ns world-sim.logs)



(defn log-entity-produce
  [world class entity action exception]
  (let [log (get-in world [:system :log])
        class-log @(get log (:class-name class))
        log-entry {:entity         (:id entity)
                   :exception      exception
                   :current-action action
                   :timestamp      @(get-in world [:physics :time])}]
    (-> class-log
        (conj log-entry)
        (->>
          (store-log world (:class-name class))))))

(defn log-entity-consume
  [world class entity action exception]
  (let [log (get-in world [:system :log])
        class-log (get @log (:class-name class))
        log-entry {:entity         (:id entity)
                   :exception      exception
                   :current-action action
                   :timestamp      @(get-in world [:physics :time])}]
    (-> class-log
        (conj log-entry)
        (->>
          (assoc @log (:class-name class))
          (store-log world)))))

(defn log-thread
  [world thread-name exception]
  (let [log (get-in world [:system :log])
        class-log @(get log :threads)
        log-entry {:thread         thread-name
                   :exception      exception
                   :timestamp      @(get-in world [:physics :time])}]
    (-> class-log
        (conj log-entry)
        (->>
          (assoc log :threads)
          (store-log world)))))

(defn store-log
  [{:keys [world class type entity action exception]}]
  (cond
    (= :thread type) (log-thread world entity exception)))