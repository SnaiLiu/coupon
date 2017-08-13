(ns server.main
  (:require [server.core :as core]
            [server.db-center :as db]
            [server.resp :as resp]
            [server.utils :as u]
            [server.resp :as resp]
            [ring.adapter.jetty :as jetty]
            [bidi.ring :refer (make-handler)]
            [ring.util.response :as res]
            [ring.middleware.params :as p]
            [ring.middleware.keyword-params :as kp]
            [clojure.tools.logging :as log]))

(defn request-event-package
  "组装一个请求事件包"
  [event-id]
  {:event-id event-id
   :result :success
   :reason nil
   :db   nil
   :resp nil})

(defn request-resources?
  "判断是否请求资源文件：html js css"
  [uri]
  (clojure.string/includes? uri "coupon_files"))

(defn handle-request
  "处理请求"
  [db-data event-id params]
  (let [event-package (request-event-package event-id)]
    (-> event-package
        (core/core-handle db-data params)
        (db/db-handle db-data)
        ;(info-log/log-handle params)
        (resp/resp-handle)
        )))

(defn routes
  "路由"
  [{:keys [db-data] :as data-src}]
  ["/" {:get {;; 查询群组信息
              ["groups/" [#"[!-~]+" :group-id]]
              (fn [req-params]
                (log/debug {:svr_method :group-info :req req-params})
                (handle-request db-data :group-info req-params))

              ["coupon_files/"]
              (bidi.ring/resources {:prefix "public/"})
              }
        :put {;; 修改用户券数量
              ["coupons"]
              (fn [req-params]
                (log/debug {:svr_method :update-user-coupon :req req-params})
                (handle-request db-data :update-user-coupon req-params))
              }}])

(defn wrap-body
  "解析body内容"
  [handler]
  (fn [req]
    (-> (assoc req :body-data (u/parse-body req))
        handler)))

(defn wrap-params
  "route参数的包装,只取业务相关的参数"
  [handler]
  (fn [{:keys [route-params body-data] :as req}]
    (if-not (request-resources? (:uri req))
      (-> (u/convert-type route-params [:username :- String])
          (merge (:params req) body-data)
          handler)
      (handler req))))

(defn status-code
  [{:keys [result data] :as resp}]
  (if (= :failed result)
    (case (:reason data)
      403)
    200))

(defn gen-resp
  "组装返回值"
  [req {:keys [result data] :as resp}]
  (-> (str resp)
      res/response
      (res/status (status-code resp))
      (res/content-type "application/edn; charset=UTF-8")))

(defn wrap-resp
  "处理发回给客户端的内容"
  [handler]
  (fn [req]
    (log/debug {:svr_method "wrap-resp" :request (str req)})
    (if-not (request-resources? (:uri req))
      (try
        (let [start (System/currentTimeMillis)
              resp (handler req)
              cost (- (System/currentTimeMillis) start)]
          (log/debug {:svr_method "wrap-resp" :response (str resp)})
          (when (> cost 200)
            (log/warn {:svr_method "wrap-resp" :cost cost :request (str req)}))
          (gen-resp req resp))
        (catch Exception e
          (log/error e {:svr_method "wrap-resp" :request (str req)})
          {:status 500 :headers {} :body ""}))
      (handler req))))

(defn wrap-login
  "登录认证"
  [handler {:keys [db-data sso-verify?] :as data-src}]
  (fn [req]
    (if sso-verify?
      (let [username (or (get-in req [:params :username])
                         (get-in req [:body-data :username]))
            token (get-in req [:cookies "hjd-token" :value])
            login? (when (and username token)
                     (db-data :sso-valid? username token))]
        (log/debug {:svr_method :wrap-login :req req :token token :username username})
        (if login?
          (handler req)
          (gen-resp req {:result :failed :data {:msg "登录过期，请重新登录。" :reason :sso-error}})))
      (handler req))))

(defn handler
  "请求处理中心"
  [{:keys [db-data sso-verify?] :as data-src}]
  (-> (make-handler (routes data-src) wrap-params)
      wrap-resp
      ;(wrap-login data-src)
      kp/wrap-keyword-params
      ;cookie/wrap-cookies
      p/wrap-params
      wrap-body))

(defn start-web-server
  "启动web服务"
  [handler web-cfg]
  (println "server start on: " web-cfg)
  (let [server (jetty/run-jetty handler web-cfg)]
    (fn [] (.stop server))))

(defn -main
  "系统启动入口"
  [config-file-name]
  (let [{:keys [outter-server db-conn] :as cfg} (u/load-edn-config config-file-name)
        db-data (db/mk-db-data :run db-conn)]
    (start-web-server (handler {:db-data db-data}) outter-server)))