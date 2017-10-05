(ns wot.app
  (:require [reagent.core :as r]
            [cljsjs.moment]
            [ajax.core :refer [GET]]))

(enable-console-print!)

(def duration (r/atom 0))

(def source-url (r/atom ""))

(def tag (r/atom "Hinkley music video experiment"))

(def playing (r/atom true))

(defn random-int [min max]
      (.floor js/Math (+ min (* (- max min) (.random js/Math)))))

(defmulti refresh!
          "get random robot+fail gif"
          (fn [& query]
              (if query
                :params
                :random)))

(defmethod refresh! :random
           []
           (GET "http://api.giphy.com/v1/gifs/random"
                {:params          {:api_key "dc6zaTOxFJmzC"
                                   :rating  "pg"
                                   :offset  (random-int 1 100)
                                   :tag     "robot+fail"}
                 :handler         #(reset! source-url
                                           (:image_original_url (:data %)))
                 :response-format :json
                 :keywords?       true}))

(defmethod refresh! :params
           [q & [txt]]
           (let [offset (random-int 1 300)]
                (GET "http://api.giphy.com/v1/gifs/search"
                     {:params          {:q       q
                                        :rating  "pg"
                                        :api_key "dc6zaTOxFJmzC"
                                        :limit   1
                                        :offset  offset}
                      :handler         (fn [{[{images :images}] :data}]
                                           (reset! source-url
                                                   ;; video loop
                                                   (-> images
                                                       :looping
                                                       :mp4)))
                                                   ;; gif loop
                                                   ;(-> images
                                                   ;    :original
                                                   ;    :url)))
                      :response-format :json
                      :keywords?       true})))


(add-watch duration :duration-watcher
           (fn [_ _ _ t]
               (when @playing
                 (case (mod t 60)
                       6 (refresh! "future")
                       12 (refresh! "robot+fail")
                       18 (refresh! "future+random")
                       24 (refresh! "future+world")
                       30 (refresh! "vr")
                       36 (refresh! "robot+dance")
                       42 (refresh! "future+fail")
                       48 (refresh! "computer+future")
                       54 (refresh! "blade+runner")
                       60 (refresh! "ai")
                       "default"))))


(defn count-up []
      (let [mm (str (.floor js/Math (/ @duration 60)) ":" (mod @duration 60))]
           [:div.count-up
            [:div mm]]))


(defn play-media [play?]
      (if play?
        (.play (js/document.querySelector "#video-el"))
        (.pause (js/document.querySelector "#video-el"))))


(defn gif-comp []
      [:div.giphy-composition
       [:div.col-lg-12
        [:video {:id       "video-el"
                 :src      @source-url
                 :autoPlay true
                 :loop     true}]]])


(defn skip-btn []
      [:button.btn.btn-danger
       {:on-click #(refresh! "the+future" (random-int 1 300))}
       [:i.fa.fa-step-forward]])

(defn play-pause-btn []
      [:button.btn.btn-danger
       {:on-click (fn []
                      (swap! playing not)
                      (play-media @playing))}
       [:i {:class (if @playing "fa fa-pause" "fa fa-play")}]])


(defn text-comp [c d]
      [:div.row.text-comp
       [:div.jumbotron
        [:p.text-center.voices
         [:span.tag @tag]]
        [c {:class "align-right"}]
        [:p.pull-right
         [play-pause-btn]
         [d]]]])

(defn main-component []
      (r/create-class
        {:component-did-mount (fn []
                                  (set! js/window.intervalFn
                                        (js/setInterval
                                          #(when @playing (swap! duration inc)) 1000))
                                  (refresh! "future" (random-int 1 300)))
         :component-will-unmount #(js/clearInterval js/window.intervalFn)
         :reagent-render      (fn [] [:div.container
                                      [gif-comp]
                                      [text-comp count-up skip-btn]
                                      [:div.footer
                                       [:p [:mark "visuals: the future according to GIPHY hashtags"]]]])}))


(defn init []
      (r/render-component [main-component]
                          (.getElementById js/document "mount")))
