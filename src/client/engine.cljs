(ns client.engine
  (:require [cljs.core.async :as async]
            [net.cgrand.xforms :as x]
            [rum.core :as rum])
  (:require-macros [cljs.core.async.macros :as async]))

(defn app-ch
  "返回程序通道: 它将使用 x-form 这个 transform 函数将每个事件 event
  转化为 view (rum 组件)，并将其渲染到 elem (HTMLElement) 上"
  [x-form elem]
  (let [input-ch (async/chan 1 x-form)]
    (async/go-loop []
      (when-let [view (async/<! input-ch)]
        (rum/mount view elem)
        (recur)))
    input-ch))

(defn map-reductions
  "返回 transform 函数：对 init-model 进行连续变换，产生新的 model 状态，
  f-map 是一个函数（map）以事件 id 为参数，返回函数将事件作用到 model 上。"
  [f-map init-model]
  (x/reductions
   (fn [model [event-id & payload]]
     (apply (f-map event-id) model payload))
   init-model))
