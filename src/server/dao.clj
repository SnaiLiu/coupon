(ns server.dao
  "数据库操作"
  (:require [clojure.java.jdbc :as sql]))

(comment
  (def mysql-db {
                 :class-name  "com.mysql.jdbc.Driver"
                 :subprotocol "mysql"
                 :subname     "//localhost:3306/coupon"
                 :user        "root"
                 :password    "111aaa"}))

(defn tables-schema
  "定义表结构"
  []
  {;; 用户表
   :users
   [[:id "SERIAL" "PRIMARY KEY"]                            ;;id
    [:name "varchar(20)" "NOT NULL UNIQUE"]                 ;;用户名
    [:update_time "varchar(60)"]]                           ;;更新时间
   ;; 群组表
   :groups
   [[:id "SERIAL" "PRIMARY KEY"]                            ;;id
    [:name "varchar(20)" "NOT NULL"]                        ;;群组名
    [:description "varchar(200)"]                           ;;群组描述
    [:update_time "varchar(60)"]]                           ;;更新时间
   ;; 卡券表
   :coupons
   [[:id "SERIAL" "PRIMARY KEY"]                            ;;id
    [:name "varchar(20)" "NOT NULL"]                        ;;卡券名称
    [:description "varchar(200)"]                           ;;卡券描述
    [:group_id :bigint "UNSIGNED"]]                         ;;关联的群组
   ;; 卡券-用户关系表
   :users_coupons
   [[:user_id :bigint "UNSIGNED"]                           ;;用户id
    [:coupon_id :bigint "UNSIGNED"]                         ;;卡券id
    [:num "INT"]                                            ;;用户拥有的卡券数量
    ["PRIMARY KEY" "(user_id, coupon_id)"]]                 ;;复合主键
   ;; 群组-用户关系表
   [[:group_id :bigint "UNSIGED"]
    [:user_id :bigint "UNSIGED"]
    ["PRIMARY KEY" "(group_id, user_id)"]]
   })

(defn init-database
  "初始化数据库"
  [tables-schema]
  (dorun (map (fn [[table-name table-def]]
                (try (sql/drop-table table-name)
                     (catch Exception e))
                (apply sql/create-table (cons table-name table-def)))
              tables-schema)))