(ns server.core)

(defn group-info
  "请求群组信息"
  [event-package db-data {:keys [group-id] :as params}]
  (let [group-coupons (db-data :group-coupons group-id)
        group-members (db-data :group-members group-id)
        group-base-info (db-data :group-base-info group-id)]
    (->> (mapv (fn [{:user/keys [name] :as user}]
                (let [user-coupons (db-data :user-coupons name)
                      group-coupon-ids (set (map :coupon/id group-coupons))
                      user-group-coupons (vec (filter #(group-coupon-ids (:coupon/id %)) user-coupons))]
                  (assoc user :user/coupons user-group-coupons)))
              group-members)
         (assoc group-base-info :group/members)
         (assoc event-package :resp))))

(defn update-user-coupon
  "更新用户的卡券"
  [event-package db-data {:keys [group-id username member-name coupon-id change-num] :as params}]
  (let [group-members (->> (db-data :group-members group-id)
                           (map :user/name)
                           set)]
    (if (and (group-members username) (group-members member-name) (not= username member-name))
      (assoc event-package :resp params :db params)
      (assoc event-package :result :failed :reason :have-no-access))))

(defn core-handle
  "核心逻辑处理模块"
  [{:keys [event-id] :as event-package} db-data params]
  (case event-id
    :group-info (group-info event-package db-data params)
    :update-user-coupon (update-user-coupon event-package db-data params)
    (assoc event-package :result :failed :reason :invalid-event)))
