(ns client.model
  (:require [client.core :as c]))

(def init-model
  {:db {:user #:user{:name nil}}})

(def event-handlers
  "事件处理函数"
  {
   :group-info c/group-info
   :group-info-resp c/group-info-resp
   :change-coupon-num c/change-coupon-num
   :cancel-change-coupon-num c/cancel-change-coupon-num
   :confirm-change-coupon-num c/confirm-change-coupon-num
   :change-coupon-num-resp c/change-coupon-num-resp
   })