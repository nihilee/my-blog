(ns cryogen.server
  (:require
    [compojure.core :refer [GET defroutes]]
    [compojure.route :as route]
    [ring.util.response :refer [redirect file-response]]
    [ring.util.codec :refer [url-decode]]
    [ring.server.standalone :as ring-server]
    [cryogen-core.watcher :refer [start-watcher! start-watcher-for-changes!]]
    [cryogen-core.plugins :refer [load-plugins]]
    [cryogen-core.compiler :refer [compile-assets-timed]]
    [cryogen-core.config :refer [resolve-config]]
    [cryogen-core.io :refer [path]]
    [clojure.string :as string])
  (:import (java.io File)))

(def resolved-config (delay (resolve-config)))

(def extra-config-dev
  "Add dev-time configuration overrides here, such as `:hide-future-posts? false`"
  {})

(defn init [& [fast?]]
  (load-plugins)
  (compile-assets-timed extra-config-dev)
  (let [ignored-files (-> @resolved-config :ignored-files)]
    (run!
      #(if fast?
         (start-watcher-for-changes! % ignored-files compile-assets-timed extra-config-dev)
         (start-watcher! % ignored-files compile-assets-timed))
      ["content" "themes"])))

(defn wrap-subdirectories
  [handler]
  (fn [request]
    (let [{:keys [clean-urls blog-prefix public-dest]} @resolved-config
          req-uri (.substring (:uri request) 1)
          res-path (if (or (.endsWith req-uri "/")
                           (.endsWith req-uri ".html")
                           (-> (string/split req-uri #"/")
                               last
                               (string/includes? ".")
                               not))
                     (condp = clean-urls
                       :trailing-slash (path req-uri "index.html")
                       :no-trailing-slash (if (or (= req-uri "")
                                                  (= req-uri "/")
                                                  (= req-uri
                                                     (if (string/blank? blog-prefix)
                                                       blog-prefix
                                                       (.substring blog-prefix 1))))
                                            (path req-uri "index.html")
                                            (path (str req-uri ".html")))
                       :dirty (path (str req-uri ".html")))
                     req-uri)
          full-path (path public-dest res-path)
          file (File. full-path)]
      (println "DEBUG: req-uri:" req-uri)
      (println "DEBUG: res-path:" res-path)
      (println "DEBUG: full-path:" full-path)
      (println "DEBUG: file exists:" (.exists file))
      (or (let [rsp (file-response res-path {:root public-dest})
                body (:body rsp)]
            (cond-> rsp
                    (and body
                         (instance? File body)
                         (string/ends-with? (.getName body) ".html"))
                    (assoc-in [:headers "Content-Type"] "text/html; charset=utf-8")
                    (and body
                         (instance? File body)
                         (string/ends-with? (.getName body) ".xml"))
                    (assoc-in [:headers "Content-Type"] "application/rss+xml; charset=utf-8")))
          (handler request))))) 

(defroutes routes
  (GET "/" [] (redirect (let [config (resolve-config)]
                          (path (:blog-prefix config)
                                (when (= (:clean-urls config) :dirty)
                                  "index.html")))))
  (route/not-found "Page not found"))

(def handler (wrap-subdirectories (route/files "/")))

(defn serve
  "Entrypoint for running via tools-deps (clojure)"
  [{:keys [fast join?] :as opts}]
  (ring-server/serve
    handler
    (merge
      {:join? (if (some? join?) join? true)
       :init (partial init fast)
       :open-browser? false
       :auto-refresh? fast ; w/o fast it would often try to reload the page before it has been fully compiled
       :refresh-paths [(:public-dest @resolved-config)]}
      opts)))

(defn -main [& args]
  (let [opts (set args)
        port (or (some-> (filter #(re-matches #"port=\d+" %) opts)
                         first
                         (subs 5)
                         Integer/parseInt)
                 3000)
        fast? (opts "fast")]
    (serve {:port port :fast fast?})))

(comment
  (def srv (serve {:join? false, :fast true}))
  (.stop srv)

  ,)