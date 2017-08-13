(ns server.db-center
  (:require [server.dao :as dao])
  (:import [java.util UUID])
  )

(defmulti db-data "数据中心统一接口，包含rmi及数据库操作"
          (fn [db-mode db-executor db-event & event-args] db-mode))

;; 实际运行模式
(defmethod db-data :run
  [_ db-executor db-event & event-args]
  (case db-event
    ;; uuid
    :uuid-str (str (UUID/randomUUID))
    ;; args: group-id
    :group-info (db-executor #(dao/group-info-by-id (first event-args)))
    ;; args: group-id
    :group-coupons (db-executor #(dao/group-coupons (first event-args)))
    ;; args: group-id
    :group-members (db-executor #(dao/group-members (first event-args)))
    ;; args: group-id
    :group-base-info (db-executor #(dao/group-base-info (first event-args)))
    ;; args: username
    :user-coupons  (db-executor #(dao/user-coupons (first event-args)))
    ;; args: username coupon_id {:num 1}
    :update-user-coupon (db-executor #(apply dao/update-user-coupon event-args))
    nil))

(defn mk-db-data
  "构建db-data数据接口"
  [db-mode db-conn]
  (let [db-executor (dao/mk-sql-executor db-conn)]
    (fn [db-event & event-args]
      (apply db-data db-mode db-executor db-event event-args))))


;;=========================================
(defn update-user-coupon
  "更新用户卡券数"
  [{:keys [db event-id] :as event-package} db-data]
  (let [{:keys [coupon-id member-name change-num]} db]
    (db-data :update-user-coupon member-name coupon-id {:num change-num})
    event-package))

(defn db-handle
  [{:keys [db event-id result] :as event-package} db-data]
  (if (and result db)
    (case event-id
      :update-user-coupon (update-user-coupon event-package db-data)
      (assoc event-package :result :failed :reason :invalid-event))
    event-package))