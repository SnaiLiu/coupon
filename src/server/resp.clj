(ns server.resp)

(defn gen-failed-msg
  "组装事件执行失败的消息"
  [reason]
  (let [msgs {:not-chairman "您不是公会会长，不能邀请。"
              :invitee-in-guild "被邀请玩家已在公会中，无法加入其他公会。"
              :invitation-not-exist "邀请函无效。"
              :has-been-invited "重复邀请，公会已向该玩家发出邀请。"
              :user-not-exist "用户名不存在，请检查后再试。"
              :invaild-action "无效操作。"}]
    (msgs reason reason)))



(defn resp-handle
  "根据处理回复消息"
  [{:keys [event-id result reason resp] :as event-package}]
  (if (= :success result)
    (let [data
          (case event-id
            (:update-user-coupon :group-info) resp)]
      {:result :success :data data})
    {:result :failed :data {:msg (gen-failed-msg reason) :reason reason}}))