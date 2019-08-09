(ns world-sim.concurrency
  (:import (java.util.concurrent Executors Future)))

(def thread-pool
  (Executors/newFixedThreadPool 8))

(defn start-thread
  [f]
  (-> (^Future future)
    (.invokeAll thread-pool f)
    (.get future)))
