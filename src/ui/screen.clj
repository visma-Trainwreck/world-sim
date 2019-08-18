(ns ui.screen
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [world-sim.mock-maps :as maps]))

(defn setup []
  ;tell quil what framerate / speed of the game and color mode, also gives the initialt state of the game.
  (q/frame-rate 120)
  (q/color-mode :rgb #_:hsb)
  (for [x (range 10)
        y (range 10)]
    [(* 20 x) (* 20 y)]))

(def size 5)

(defn draw
  [state]
  (q/clear)
  (let [tiles (get-in maps/world [:landmasses :enviroment :pool])
        trees-birch @(get-in maps/world [:gaia :trees :pool :birch :pool])
        trees-oak @(get-in maps/world [:gaia :trees :pool :oak :pool])
        tree-elm @(get-in maps/world [:gaia :trees :pool :elm :pool])
        animals @(get-in maps/world [:living-entities :animal :pool :horse :pool])]
         (q/fill 255 255 255)
         (q/text-size 50)
         (q/text (str "year: " @(get-in maps/world [:physics :time])) 2100 90)
         (q/text (str "framerate: " (q/current-frame-rate)) 2100 200)

         (doseq [[key animal] animals]
           (let [x (get-in animal [:location :x])
                 y (get-in animal [:location :y])]
             (if (:death-date animal)
               (q/fill 150 150 150)
               (q/fill 255 100 100))
             (q/rect (* size x) (* size y) size size)))

         (doseq [[key tree] trees-birch]
             (let [x (get-in tree [:location :x])
                   y (get-in tree [:location :y])]
               (if (:death-date tree)
                 (q/fill 150 150 150)
                 (q/fill 255 100 100))
               (q/rect (* size x) (* size y) size size)))
         (doseq [[key tree] trees-oak]
             (let [x (get-in tree [:location :x])
                   y (get-in tree [:location :y])]
               (if (:death-date tree)
                 (q/fill 150 150 150)
                 (q/fill 100 255 100))
               (q/rect (* size x) (* size y) size size)))
         (doseq [[key tree] tree-elm]
           (let [x (get-in tree [:location :x])
                 y (get-in tree [:location :y])]
             (if (:death-date tree)
               (q/fill 150 150 150)
               (q/fill 100 100 255))
             (q/rect (* size x) (* size y) size size)))

         )
  #_(q/fill 255 100 100)
  #_(doseq [coords state
          :let [x (first coords)
                y (second coords)]]
    (q/rect x y 20 20)))

(defn updater
  [state]
  state)

(defn show-window
  []
  (q/defsketch placeholder
               :title "simulator"
               :size [2560 1440]
               :setup setup
               :draw draw
               :update updater
               :features [:keep-on-top]
               :middleware [m/fun-mode]))