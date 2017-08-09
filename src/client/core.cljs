(ns client.core)

;;==============================
;;
(defn request-group-info
  "请求群组信息"
  [group-id]
  {:method :get
   :uri    (str "/groups/" group-id)
   :params nil
   :resp-event :group-info-resp})

(defn request-change-coupon-num
  "请求更改卡券"
  [{:keys [group-id coupon-id member-name coupon-name change-num] :as curr-change-coupon}]
  {:method     :put
   :uri        "/coupons"
   :params     curr-change-coupon
   :resp-event :change-coupon-num-resp})

;;==============================
;; core handlers
(defn group-info
  "群组信息"
  [{:keys [db] :as app-data} group-id]
  (-> app-data
      (assoc :http (request-group-info group-id))))

(defn group-info-resp
  "群组信息回复"
  [{:keys [db] :as app-data} group]
  (println "group-info-resp")
  (-> app-data
      (assoc-in [:db :curr-group] group)))

(defn gen-curr-change-coupon
  [group-id member-name coupon-id coupon-name change-num]
  {:group-id group-id
   :member-name member-name
   :coupon-id coupon-id
   :coupon-name coupon-name
   :change-num change-num})

(defn change-coupon-num
  "修改卡券数量"
  [{:keys [db] :as app-data} group-id member-name card-id card-name change-num]
  (let [curr-change-coupon (gen-curr-change-coupon group-id member-name card-id card-name change-num)]
    (-> app-data
        (assoc-in [:db :curr-change-coupon] curr-change-coupon))))

(defn cancel-change-coupon-num
  "取消修改卡券数量"
  [{:keys [db] :as app-data}]
  (-> app-data
      (assoc-in [:db :curr-change-coupon] nil)))

(defn confirm-change-coupon-num
  "确定更改卡券数量"
  [{:keys [db] :as app-data}]
  (-> app-data
      (assoc :http (request-change-coupon-num (:curr-change-coupon db)))))

(defn change-coupon-num-resp
  [{:keys [db] :as app-data} {:keys [group-id coupon-id member-name coupon-name change-num]}]
  (-> app-data
      (update-in [:db :curr-group :group/members member-name
                  :user/coupons coupon-name :coupon/num] + change-num)
      (assoc-in [:db :curr-change-coupon] nil)))
