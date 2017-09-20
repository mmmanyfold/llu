(ns wot.app
  (:require [reagent.core :as r]
            [ajax.core :refer [GET]]))

(enable-console-print!)

(def duration (r/atom 0))

(def source-url (r/atom ""))

(def voice-1 (r/atom "Impact360 music video experiment"))

(defn get-random-int [min max]
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
                                   :tag     "robot+fail"}
                 :handler         #(reset! source-url
                                           (:image_original_url (:data %)))
                 :response-format :json
                 :keywords?       true}))

(defmethod refresh! :params
           [q & [offset txt]]
           (let [off (get-random-int 1 12)]
                (if txt (reset! voice-1 txt))
                (GET "http://api.giphy.com/v1/gifs/search"
                     {:params          {:q       q
                                        :api_key "dc6zaTOxFJmzC"
                                        :limit   1
                                        :offset  (or offset 0)}
                      :handler         #(reset! source-url
                                                ;; (:mp4 (:looping (:images (first (:data res))))) ;; video-loop
                                                (:url (:original (:images (first (:data %))))))
                      :response-format :json
                      :keywords?       true})))


(add-watch duration :duration-watcher
           (fn [_ _ _ t]
               (case t

                     10 (refresh! "dolphin" 2)

                     20 (refresh! "robot+fail" 2)

                     30 (refresh! "gold+magic")

                     40 (refresh! "harambe" 11)

                     50 (refresh! "robot+dance" 12)

                     60 (refresh! "robot+fail" 30)

                     "default")))


(defn count-up []
      (let [mm (str (.floor js/Math (/ @duration 60)) ":" (mod @duration 60))]
           [:div.count-up
            [:div @duration]]))

(defn gif-comp []
      [:div.row.gifComp
       [:div.col-lg-12
        [:img {:src @source-url}]]])

(defn audio-comp []
      [:div.row.audio-tag
       [:div.col-lg-12
        [:audio {:src      "media/needings.wav"
                 :id       "audio-el"
                 :autoPlay false}]]])

(defn play-btn []
      [:button.btn.btn-danger
       {:on-click (fn []
                      (.play (.querySelector js/document "#audio-el"))
                      (js/setInterval #(swap! duration inc) 1000))}
       "play"])

(defn pause-btn []
      [:button.btn.btn-danger
       {:on-click (fn []
                      (.pause (.querySelector js/document "#audio-el")))}
       "pause"])

(defn text-comp [c b d]
      [:div.row.text-comp
       [:div.col-lg-12.jumbotron
        [:p.text-center.voices
         [:em @voice-1]]
        [c {:class "align-right"}]
        [:p.pull-right
         [b]
         [d]]]])

(defn main-component []
      (let []
           (r/create-class
             {:component-did-mount #(refresh! "robot+dance" 2)
              :component-will-unmount #(js/console.log "out")
              :reagent-render      (fn [] [:div.container

                                           [gif-comp]
                                           [audio-comp]
                                           [text-comp count-up play-btn pause-btn]])})))


(defn init []
      (r/render-component [main-component]
                          (.getElementById js/document "mount")))