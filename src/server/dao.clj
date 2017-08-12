(ns server.dao
  "数据库操作"
  (:require [clojure.java.jdbc :as sql])
  (:import [java.util UUID]))


(defn tables-schema
  "定义表结构"
  []
  {;; 用户表
   :users
   [[:name "varchar(20)" "PRIMARY KEY" "NOT NULL UNIQUE"]                 ;;用户名
    [:update_time "varchar(60)"]]                           ;;更新时间
   ;; 群组表
   :groups
   [[:id "varchar(36)" "PRIMARY KEY"]                            ;;id(uuid)
    [:name "varchar(20)" "NOT NULL"]                        ;;群组名
    [:description "varchar(200)"]                           ;;群组描述
    [:update_time "varchar(60)"]]                           ;;更新时间
   ;; 卡券表
   :coupons
   [[:id "SERIAL" "PRIMARY KEY"]                            ;;id
    [:name "varchar(20)" "NOT NULL"]                        ;;卡券名称
    [:description "varchar(200)"]                           ;;卡券描述
    [:group_id "varchar(36)"]]                         ;;关联的群组
   ;; 卡券-用户关系表
   :users_coupons
   [[:username "varchar(20)"]                           ;;用户名
    [:coupon_id "varchar(36)"]                         ;;卡券id
    [:num "INT"]                                            ;;用户拥有的卡券数量
    ["PRIMARY KEY" "(username, coupon_id)"]]                 ;;复合主键
   ;; 群组-用户关系表
   :groups_users
   [[:group_id "varchar(36)"]
    [:username "varchar(36)"]
    ["PRIMARY KEY" "(group_id, username)"]]
   })

(defn init-database
  "初始化数据库"
  [tables-schema]
  (dorun (map (fn [[table-name table-def]]
                (try (sql/drop-table table-name)
                     (catch Exception e
                       (prn "e == " e)))
                (apply sql/create-table (concat [table-name] table-def [ :table-spec "DEFAULT CHARSET=utf8"])))
              tables-schema)))

(defn add-group
  "添加一个群组"
  [group-id group-name group-description update-time]
  (sql/insert-rows :groups
                     ;[:id :name :description :update_time]
                     [group-id group-name group-description update-time]))

(comment
  (def mysql-db {
                 :class-name  "com.mysql.jdbc.Driver"
                 :subprotocol "mysql"
                 :subname     "//localhost:3306/coupon?useUnicode=true&characterEncoding=utf8"
                 :user        "root"
                 :password    "111aaa"})
  (sql/with-connection mysql-db
                       (init-database (tables-schema)))

  (sql/with-connection mysql-db
                       (add-group (str (UUID/randomUUID)) "柳朕&姜琳琳" "相亲相爱的一家人"
                                  (java.sql.Timestamp. (System/currentTimeMillis))))
  )