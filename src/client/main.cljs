(ns client.main
  (:require
    [client.view :as v]
    [client.core :as c]
    [client.model :as m]
    [client.engine :as e]
    [cljs.core.async :as async]
    [ajax.core :as ajax]
    [cljs.reader]))

(enable-console-print!)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; http请求
(defmulti http-request "http请求" :run-mode)

(defmethod http-request :default
  [app-data]
  (.log js/console "invalid run-mode! supported mode: :local or :network"))

(defmethod http-request :local
  [{:keys [dispatch http] :as app-data}]
  (let [local-data {:login-resp {:username (get-in http [:params :username])
                                 :my-groups [{:group-name "lz-love-jll"}
                                             {:group-name "我的群组3"}]}
                    :group-info-resp #:group{:name    "柳朕&姜琳琳"
                                             :id      123456
                                             :members {"柳朕"  #:user{:name    "柳朕"
                                                                    :coupons {"洗衣券" #:coupon{:id 1234 :name "洗衣券" :desc "包括洗、晾衣服" :num 1}
                                                                              "洗碗券" #:coupon{:id 2345 :name "洗碗券" :desc "包括洗碗、擦灶台" :num 2}}}
                                                       "姜琳琳" #:user{:name    "姜琳琳"
                                                                    :coupons {"洗衣券" #:coupon{:id 1234 :name "洗衣券" :desc "包括洗、晾衣服" :num 2}
                                                                              "洗碗券" #:coupon{:id 2345 :name "洗碗券" :desc "包括洗碗、擦灶台" :num 3}}}}}
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
    (dispatch [:group-info "123456"])
    ch))


(-main :local)
