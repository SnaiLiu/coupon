(ns client.view
  (:require [rum.core :as rum :include-macros true :refer [defc]]
            [cljs-react-material-ui.icons :as ic]
            [cljs-react-material-ui.core :refer [get-mui-theme color]]
            [cljs-react-material-ui.rum :as ui]
            [client.common-components :as cc]))

(defc of-change-coupon-num
  "修改卡券数量"
  [dispatch curr-change-coupon]
  (let [{:keys [member-name coupon-name change-num]} curr-change-coupon
        change-str (if (neg? change-num) change-num (str "+" change-num))]
    (cc/confirm-dialog "更改卡券数量"
                       (str "确定给 " member-name " 的 " coupon-name change-str "?")
                       #(dispatch [:cancel-change-coupon-num])
                       #(dispatch [:confirm-change-coupon-num curr-change-coupon]))))

(defc of-user-coupons
  "用户的卡券列表"
  [dispatch group-id username curr-user-coupons]
  [:div.user-coupons {:key (str group-id "-" (:user/name curr-user-coupons))}
   (for [{:coupon/keys [id name desc num] :as coupon} (vals (:user/coupons curr-user-coupons))]
     (let [member-name (:user/name curr-user-coupons)]
       (ui/card {:style {:margin-top "5px"} :key name}
                (ui/card-header {:title name :subtitle desc :avatar "img/couponAvator.png"})
                (ui/card-text (str "当前数量：" num))
                (when-not (= username member-name)
                  (ui/card-actions
                    (ui/raised-button {:primary  true
                                       :on-click #(dispatch [:change-coupon-num group-id member-name id name 1])}
                                      "INC+1")
                    (ui/raised-button {:secondary true
                                       :on-click  #(dispatch [:change-coupon-num group-id member-name id name -1])}
                                      "DEC-1"))))))])

(defc of-group-info
  "群组信息页面"
  [dispatch username {:group/keys [name id members] :as curr-group}]
  [:div.curr-group
   (cc/app-bar :detail-bar name #(dispatch [:close-curr-group]) nil)
   (ui/list {:default-value (count (keys members))
             :id            "member-list"}
            (for [[member-name member-info] members]
              (ui/list-item {:primaryText member-name
                             :key         (str name "-" member-name)
                             :style       {:color (if (= username member-name) "red" "black")}
                             :leftAvatar  (ui/avatar {:src "img/userAvator.png"})
                             :nestedItems [(of-user-coupons dispatch id username member-info)]})))])


(defc of-model
  [{:keys [dispatch module-states curr-group user curr-change-coupon] :as model}]
  (let [{:keys [curr-group-open?]} module-states
        username (:user/name user)]
    (ui/mui-theme-provider
      {:mui-theme (get-mui-theme)}
      [:div
       (when curr-group
         (of-group-info dispatch username curr-group))
       (when curr-change-coupon
         (of-change-coupon-num dispatch curr-change-coupon))])))

(comment
  (def model
    {:user               #:user{:name "柳朕"}
     :module-states      {:curr-group-open? true}
     :curr-group         #:group{:name    "柳朕&姜琳琳"
                                 :id      123456
                                 :members {"柳朕"  #:user{:name    "柳朕"
                                                        :coupons {"洗衣券" #:coupon{:id 1234 :name "洗衣券" :desc "包括洗、晾衣服" :num 1}
                                                                   "洗碗券" #:coupon{:id 2345 :name "洗碗券" :desc "包括洗碗、擦灶台" :num 2}}}
                                           "姜琳琳" #:user{:name    "姜琳琳"
                                                        :coupons {"洗衣券" #:coupon{:id 1234 :name "洗衣券" :desc "包括洗、晾衣服" :num 2}
                                                                  "洗碗券" #:coupon{:id 2345 :name "洗碗券" :desc "包括洗碗、擦灶台" :num 3}}}}}
     :curr-change-coupon {:group-id 12345 :member-name "姜琳琳" :coupon-id 1234 :coupon-name "洗衣券" :change-num -1}
     :dispatch           #(js/alert %)})
  (rum/mount (of-model model) (.getElementById js/document "app")))

