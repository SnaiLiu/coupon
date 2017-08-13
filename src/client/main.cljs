(ns client.main
  (:require
    [client.view :as v]
    [client.core :as c]
    [client.model :as m]
    [client.engine :as e]
    [cljs.core.async :as async]
    [ajax.core :as ajax]
    [ajax.edn :as aedn]
    [cljs.reader]))

(enable-console-print!)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; http请求
(defmulti http-request "http请求" :run-mode)

(defmethod http-request :default
  [app-data]
  (.log js/console "invalid run-mode! supported mode: :local or :network"))


(defmethod http-request :network
  [{:keys [dispatch http] :as app-data}]
  (let [{:keys [method uri params resp-event]} http
        base-uri "http://127.0.0.1:8088"
        fn-handle (fn [[success? resp]]
                    (if success?
                      (dispatch [resp-event (:data resp) #_(cutils/change-key-join-line resp "_" "-")])
                      (dispatch [:handle-error-resp "系统繁忙，请稍后再试"])))]
    (when http
      (ajax/ajax-request
        {:uri             (str base-uri uri)
         :method          method
         :params          params
         :handler         fn-handle
         :format          (aedn/edn-request-format)
         :response-format (aedn/edn-response-format)}))
    app-data))

(defmethod http-request :local
  [{:keys [dispatch http] :as app-data}]
  (let [local-data {:login-resp {:username (get-in http [:params :username])
                                 :my-groups [{:group-name "lz-love-jll"}
                                             {:group-name "我的群组3"}]}
                    :group-info-resp #:group{:name    "柳朕&姜琳琳"
                                             :id      123456
                                             :members [#:user{:name    "柳朕"
                                                              :coupons [#:coupon{:id 1234 :name "洗衣券" :description "包括洗、晾衣服" :num 1}
                                                                        #:coupon{:id 2345 :name "洗碗券" :description "包括洗碗、擦灶台" :num 2}]}
                                                       #:user{:name    "姜琳琳"
                                                              :coupons [#:coupon{:id 1234 :name "洗衣券" :description "包括洗、晾衣服" :num 2}
                                                                        #:coupon{:id 2345 :name "洗碗券" :description "包括洗碗、擦灶台" :num 3}]}]}
                    :change-coupon-num-resp (:params http)}
        resp-event (:resp-event http)]
    (when http
      (js/setTimeout #(dispatch [resp-event (local-data resp-event)]) 1000))
    app-data))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 集成
(defn wrap-app-dispatcher
  [event-id]
  (let [f-boot #(assoc %1 :dispatch %2)
        handlers (assoc m/event-handlers ::boot f-boot)]
    (fn [app-data & event-data]
      (apply (handlers event-id) (dissoc app-data :http) event-data))))

(defn ^:export -main
  "入口函数。run-mode为:local时，表示使用本地模式，无网络调用。为:network时，则表示有网络调用"
  [run-mode]
  (let [local-cards (.getItem js/localStorage "card-storage")
        x-form (comp
                 (e/map-reductions wrap-app-dispatcher m/init-model) ;转换状态
                 (map #(http-request (assoc % :run-mode run-mode)))
                 (map #(assoc (:db %) :dispatch (:dispatch %)))
                 (map v/of-model))
        elem (.getElementById js/document "app")
        ch (e/app-ch x-form elem)
        dispatch (partial async/put! ch)]
    (dispatch [::boot dispatch])
    (dispatch [:group-info "bf67756e-ecb7-4eb3-86df-ec153bf03f22"])
    ch))


(-main :network)
