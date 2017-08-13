(ns client.common-components
  "一些通用的显示组件"
  (:require [rum.core :as rum :include-macros true :refer [defc]]
            [cljs-react-material-ui.icons :as ic]
            [cljs-react-material-ui.core :refer [get-mui-theme color]]
            [cljs-react-material-ui.rum :as ui]))

(defc app-bar
  "通用的应用的bar"
  [bar-type title left-icon-touch-fn icon-element-right]
  (let [icon-left (case bar-type
                    :detail-bar (ui/icon-button (ic/navigation-close))
                    (ui/icon-button (ic/navigation-menu)))]
    (ui/app-bar {:title                    title
                 :onLeftIconButtonTouchTap left-icon-touch-fn
                 :iconElementLeft          icon-left
                 :iconElementRight         icon-element-right})))

(defn action-btn-pair
  "按钮对"
  [first-label second-label first-fn second-fn]
  (let [first-btn (ui/flat-button {:label first-label :primary false :on-click first-fn})
        second-btn (ui/flat-button {:label second-label :primary true :on-click second-fn})]
    [first-btn second-btn]))

(defn confirm-dialog
  "确认框"
  [title text cancel-fn ok-fn]
  (ui/dialog {:title       title
              :title-style {:color "green"}
              :open        true
              :actions     (action-btn-pair "取消" "确认" cancel-fn ok-fn)}
             text))
