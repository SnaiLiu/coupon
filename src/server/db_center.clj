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
    nil))

(defn mk-db-data
  "构建db-data数据接口"
  [db-mode db-conn]
  (let [db-executor (dao/mk-sql-executor db-conn)]
    (fn [db-event & event-args]
      (apply db-data db-mode db-executor db-event event-args))))