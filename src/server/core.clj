(ns server.core)

(defn group-info
  "请求群组信息"
  [event-package db-data {:keys [username group-id] :as params}]
  (let [group-info (db-data :group-info group-id)]
    (assoc event-package :resp group-info)))

(defn update-user-coupons
  "更新用户的卡券"
  [event-package db-data {:keys [group-id member-name coupon-id coupon-name change-num] :as params}]
  (let [])
  (assoc event-package :resp params :db params))

(defn core-handle
  "核心逻辑处理模块"
  [{:keys [event-id] :as event-package} db-data params]
  (case event-id
    :group-info (group-info event-package db-data params)
    (assoc event-package :result :failed :reason :invalid-event)))
