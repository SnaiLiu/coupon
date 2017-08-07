(defproject coupon "0.1.0-SNAPSHOT"
  :description "酷嘭！！——生活的'优惠券'"
  :url "http://coupon.thinkou.com/index.html"
  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                 ;; web server
                 [bidi "2.1.1" :exclusions [ring/ring-core]]
                 [ring "1.6.1"]
                 ;; json log
                 [org.clojure/tools.logging "0.4.0"]
                 [cheshire "5.7.1"]
                 [ch.qos.logback/logback-classic "1.2.3"]
                 [net.logstash.logback/logstash-logback-encoder "4.10"]]

  :profiles {:dev {:resource-paths ["etc" "resources"]
                   :plugins        [[lein-cljsbuild "1.1.6" :exclusions [[org.clojure/clojure]]]
                                    [lein-figwheel "0.5.10"]]
                   :dependencies   [[org.clojure/clojure "1.9.0-alpha17"]

                                    [figwheel-sidecar "0.5.10"]
                                    [com.cemerick/piggieback "0.2.1"]
                                    [org.clojure/clojurescript "1.9.229"]
                                    [org.clojure/core.async "0.3.442"]

                                    [rum "0.10.8" :exclusions [cljsjs/react cljsjs/react-dom]]
                                    ;react 包装的显示层库
                                    [datascript "0.16.1"] ;内存数据库管理模型
                                    [cljs-react-material-ui "0.2.44"] ;UI 组件库
                                    [net.cgrand/xforms "0.9.2"] ;更多的 transducer 函数
                                    [binaryage/devtools "0.9.2"]

                                    [cljs-ajax "0.6.0"]]
                   :source-paths  ["src"]
                   :clean-targets ^{:protect false} ["resources/public/scripts"
                                                     :target-path]
                   :cljsbuild        {:builds
                                      [{:id           "dev"
                                        :source-paths ["src"]
                                        :figwheel     {:open-urls ["http://localhost:3449/index.html"]
                                                       :on-jsload "client.main/-main"}
                                        :compiler     {:main                 client.main
                                                       :asset-path           "js/out"
                                                       :output-to            "resources/public/js/coupon_0.1.0.js"
                                                       :output-dir           "resources/public/js/out"
                                                       :source-map-timestamp true
                                                       :preloads             [devtools.preload]
                                                       }}
                                       {:id           "min"
                                        :source-paths ["src"]
                                        :compiler     {:output-to     "resources/public/js/coupon_0.1.0.js"
                                                       :main          client.main
                                                       :optimizations :advanced
                                                       :pretty-print  false}}]}}}
  )
