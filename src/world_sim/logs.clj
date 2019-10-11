(ns world-sim.logs)

(defn log-entity-produce
  [{:keys [world entity-class entity action exception]}]
  {:entity    entity
   :class     (:class-name entity-class)
   :action    action
   :exception exception
   :timestamp @(get-in world [:physics :time])})

(defn log-entity-consume
  [{:keys [world entity-class entity action exception]}]
  {:entity    entity
   :class (:class-name entity-class)
   :action action
   :exception exception
   :timestamp @(get-in world [:physics :time])})

(defn create-thread-log
  [{:keys [world entity exception]}]
  {:thread-name    entity
   :exception exception
   :timestamp @(get-in world [:physics :time])})

(defn exception-logging
  [{:keys [exception]} logs]
  (if exception
    (swap! (:exceptions logs) (fn [exceptions] (conj exceptions )))))

(defn update-atom
  [ctx class-log-ref fx]
  (let [log-file (fx ctx)]
    (exception-logging ctx class-log-ref)
    (swap! class-log-ref
           (fn [class-log] (->> log-file
                             (conj class-log))))))

(defn store-log
  [{:keys [world entity-class type] :as ctx}]
  (let [logs (get-in world [:system :logs])]
    (cond
      (= :thread type) (update-atom ctx (get logs :threads) create-thread-log)
      (= :producer type) (update-atom ctx
                                      (get-in logs [:producers (:class-name entity-class)])
                                      log-entity-produce)
      (= :consumer type) (update-atom ctx
                                      (get-in logs [:consumers (:class-name entity-class)])
                                      log-entity-consume))))