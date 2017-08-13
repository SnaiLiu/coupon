(ns server.dao
  "数据库操作"
  (:require [clojure.java.jdbc :as sql]
            [server.utils :as u])
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
   [[:id "varchar(36)" "PRIMARY KEY"]                            ;;id
    [:name "varchar(20)" "NOT NULL"]                        ;;卡券名称
    [:description "varchar(200)"]                           ;;卡券描述
    [:group_id "varchar(36)"]
    [:update_time "varchar(60)"]]                         ;;关联的群组
   ;; 卡券-用户关系表
   :users_coupons
   [[:username "varchar(20)"]                           ;;用户名
    [:coupon_id "varchar(36)"]                          ;;卡券id
    [:num "INT"]                                        ;;用户拥有的卡券数量
    [:update_time "varchar(60)"]
    ["PRIMARY KEY" "(username, coupon_id)"]]                 ;;复合主键
   ;; 群组-用户关系表
   :groups_users
   [[:group_id "varchar(36)"]
    [:username "varchar(36)"]
    [:update_time "varchar(60)"]
    ["PRIMARY KEY" "(group_id, username)"]]
   })

(defn init-database
  "初始化数据库"
  [tables-schema]
  (dorun (map (fn [[table-name table-def]]
                (try (sql/drop-table table-name)
                     (catch Exception e))
                (apply sql/create-table (concat [table-name] table-def [ :table-spec "DEFAULT CHARSET=utf8"])))
              tables-schema)))

(defn add-group
  "添加一个群组"
  [group-id group-name group-description update-time]
  (sql/insert-rows :groups
                     ;[:id :name :description :update_time]
                     [group-id group-name group-description update-time]))

(defn add-user
  "添加一个用户"
  [username update-time]
  (sql/insert-rows :users [username update-time]))

(defn add-group-user
  "添加群组-用户关系"
  [group-id username update-time]
  (sql/insert-rows :groups_users
                   [group-id username update-time]))

(defn add-coupon
  "新增一张券"
  [coupon-id name description group-id update-time]
  (sql/insert-rows :coupons
                   [coupon-id name description group-id update-time]))

(defn add-user-coupon
  "为某个玩家新建一张券"
  [username coupon-id num update-time]
  (sql/insert-rows :users_coupons
                   [username coupon-id num update-time]))

(defn mk-sql-executor
  "创建sql执行器"
  [sql-db]
  (fn [sql-fn]
    (sql/with-connection sql-db (sql-fn))))

(defn group-base-info
  "查询群组信息"
  [group-id]
  (->> (sql/with-query-results rs ["select * from groups where id=?" group-id]
                               (first (vec rs)))
       (u/add-ns "group")))

(defn group-coupons
  "群组有的卡券"
  [group-id]
  (->> (sql/with-query-results rs ["select id, name, description from coupons where group_id=?;" group-id]
                               (vec rs))
       (mapv #(u/add-ns "coupon" %))))

(defn group-members
  "查询群组成员列表"
  [group-id]
  (->> (sql/with-query-results rs ["select username from groups_users where group_id=?"
                                 group-id]
                             (vec rs))
       (mapv (fn [user]
               {:user/name (:username user)}))))

(defn user-coupons
  "查询用户的奖券"
  [username]
  (->> (sql/with-query-results rs ["select users_coupons.num, coupons.id, coupons.description, coupons.name
  from users_coupons left join coupons on users_coupons.coupon_id = coupons.id
  where users_coupons.username=?" username]
                               (vec rs))
       (mapv #(u/add-ns "coupon" %))))

(defn group-info-by-id
  "查询群组信息"
  [group-id]
  (let [base-info (group-base-info group-id)
        members (group-members group-id)]
    (assoc base-info :members members)))

(comment
  (defn add-coupons
    [group-id]
    (let [members (group-members group-id)
          update-time (java.sql.Timestamp. (System/currentTimeMillis))
          c-laundry [(str (UUID/randomUUID)) "洗衣券" "包括洗、晾衣服。" group-id update-time]
          c-dish [(str (UUID/randomUUID)) "洗碗券" "包括洗碗、擦灶台。" group-id update-time]
          c-cook [(str (UUID/randomUUID)) "烹饪券" "最少2个菜。" group-id update-time]
          c-mop [(str (UUID/randomUUID)) "拖地券" "包括扫地、拖地。" group-id update-time]
          c-massage [(str (UUID/randomUUID)) "按摩券" "全身按摩。" group-id update-time]
          c-clean [(str (UUID/randomUUID)) "整理券" "整理杂物、擦家具。" group-id update-time]
          c-pet [(str (UUID/randomUUID)) "动物美容券" "替娘口洗澡、吹干。" group-id update-time]
          c-hhh [(str (UUID/randomUUID)) "娱乐生活券" "嘿嘿嘿。" group-id update-time]
          c-absolution [(str (UUID/randomUUID)) "免罪券" "立马不生气。" group-id update-time]
          c-outdoor [(str (UUID/randomUUID)) "出门券" "使用前提醒对方洗澡。" group-id update-time]
          c-sports [(str (UUID/randomUUID)) "运动券" "打球、健身等，持续一小时。" group-id update-time]
          c-study [(str (UUID/randomUUID)) "学习券" "每券学习2小时。" group-id update-time]
          c-game [(str (UUID/randomUUID)) "游戏休闲券" "包含各类娱乐游戏。" group-id update-time]
          c-shopping [(str (UUID/randomUUID)) "买买买券" "女方独有。" group-id update-time]
          c-company [(str (UUID/randomUUID)) "陪伴券" "陪伴学习2小时。" group-id update-time]
          insert-row (fn [usernames coupon]
                       (dorun (map #(apply sql/insert-rows
                                           [:users_coupons [(:username %) (first coupon) 0 update-time]])
                                   usernames)))]
      (sql/insert-rows :coupons
                       c-laundry c-dish c-cook c-mop c-massage c-clean c-pet c-hhh c-absolution
                       c-outdoor c-sports c-study c-game c-shopping c-company)
      (dorun (map #(insert-row members %)
                  [c-laundry c-dish c-cook c-mop c-massage c-clean c-pet c-hhh c-absolution
                   c-outdoor c-sports c-study c-game c-shopping c-company]))))

  (let [mysql-db {:class-name  "com.mysql.jdbc.Driver"
                  :subprotocol "mysql"
                  :subname     "//localhost:3306/coupon?useUnicode=true&characterEncoding=utf8"
                  :user        "root"
                  :password    "111aaa"}
        now-time-fn (fn [] (java.sql.Timestamp. (System/currentTimeMillis)))
        group-id (str (UUID/randomUUID))
        exe-sql (mk-sql-executor mysql-db)]
    ;; 创建表
    (exe-sql #(init-database (tables-schema)))
    ;; 添加用户
    (exe-sql #(add-user "柳朕" (now-time-fn)))
    (exe-sql #(add-user "姜琳琳" (now-time-fn)))
    ;; 创建群组
    (exe-sql #(add-group group-id "柳朕&姜琳琳" "相亲相爱的一家人" (now-time-fn)))
    ;; 群组添加成员
    (exe-sql #(add-group-user group-id "柳朕" (now-time-fn)))
    (exe-sql #(add-group-user group-id "姜琳琳" (now-time-fn)))
    ;; 查询群组信息
    (prn "group-info~~~~" (exe-sql #(group-info-by-id group-id)))
    ;;
    (exe-sql #(add-coupons group-id))
  ))