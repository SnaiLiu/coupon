(ns server.utils
  (:require [clojure.edn :as edn]
            [clojure.java.io :as cio]
            [ring.util.codec :as codec])
  (:import (java.io PushbackReader)))

(defn add-ns
  "为一个map加上命名空间ns"
  [ns-str a-map]
  (->> (str "#:" ns-str a-map)
       (edn/read-string)))

(defn load-edn-config
  "加载edn配置"
  [edn-filename]
  (with-open [in-edn (-> edn-filename
                         cio/resource
                         cio/reader
                         (PushbackReader.))]
    (edn/read in-edn)))

(defn convert-type
  "转换map中val的类型"
  [m types]
  (let [fm {Long   #(Long/parseLong %)
            String #(codec/url-decode %)}
        param-pairs (partition 3 types)]
    (reduce
      (fn [curr [k _ type]]
        (if-let [v (k curr)]
          (assoc curr k ((get fm type identity) v))
          curr))
      m param-pairs)))

(defn body->str
  "http请求的body数据转换为字符串"
  [body]
  (if body
    (slurp body)
    ""))

(defn str->edn
  "edn形式的字符串转换为edn格式"
  [string]
  (edn/read-string string))

(defn parse-body
  "解析http请求body的数据为edn格式,数据不规范时该函数可能会抛出异常,需要调用者处理"
  [{:keys [headers body] :as req}]
  (let [body-str (body->str body)
        content-type (get headers "content-type")]
    (when content-type
      (cond
        (clojure.string/includes? content-type "application/edn") (str->edn body-str)
        :default
        body-str))))
