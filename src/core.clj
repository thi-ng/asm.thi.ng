(ns core
  (:require
   [thi.ng.strf.core :as f]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [hiccup.core :refer [html]]
   [hiccup.page :refer [html5]]))

(def config (read-string (slurp (io/resource "content.edn"))))

(def link-count
  (->> (:sections config)
       (vals)
       (mapcat :categories)
       (mapcat :links)
       (count)))

(defn inject-variables
  [body]
  (let [now (java.util.Date.)]
    (-> body
        (str/replace "__TIMESTAMP__" (str (.getTime now)))
        (str/replace "__DATE__" (f/format-date now))
        (str/replace "__COUNT__" (str link-count)))))

(defn page-header
  [{:keys [title meta css js]}]
  (let [ts (str (.getTime (java.util.Date.)))]
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:content "IE=edge" :http-equiv "X-UA-Compatible"}]
     (for [[id content] meta]
       [:meta {:name id :content content}])
     [:title title]
     (for [uri css]
       [:link {:href (str/replace uri "__CACHE__" ts) :rel "stylesheet" :type "text/css"}])
     (for [uri js]
       [:script {:src (str/replace uri "__CACHE__" ts) :type "text/javascript"}])]))

(defn asm-link
  [indent {:keys [url label desc]}]
  (let [link (list indent ".asciz \"" [:a {:href url} label] "\"")]
    (if desc
      (concat link ["\n" indent "@ " desc "\n"])
      (list link "\n"))))

(defn asm-block-comment
  [comment]
  (str (str/join "\n" (for [l (str/split comment #"\n")] (str "@@ " l))) "\n"))

(defn page-section
  [id {:keys [section-label body desc categories] :as section} config]
  [:pre {:id (name id)}
   [:code.armasm
    (if body
      (inject-variables body)
      (concat
       (when section-label (str (:indent config) ".section " section-label "\n\n"))
       (when desc (str (asm-block-comment desc) "\n"))
       (mapcat
        (fn [cat]
          (let [links (interpose "\n" (map #(asm-link (:indent config) %) (:links cat)))]
            (if-not (empty? (:label cat))
              (list (:label cat) ":\n" links "\n")
              (list links "\n"))))
        categories)
       ["\n"]))]])

(defn page-nav
  [nav sections]
  [:nav
   "@@ "
   (interpose " / " (for [id (:items nav)] [:a.nav {:href (str "#" (name id))} id]))])

(defn generate-page
  [{:keys [html order sections tracking] :as config}]
  (html5
   (page-header html)
   [:body
    (for [id order]
      (if (= :nav id)
        (page-nav (:nav sections) sections)
        (page-section id (id sections) config)))
    [:script "hljs.initHighlightingOnLoad();"]
    (when tracking
      [:script "(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)})(window,document,'script','//www.google-analytics.com/analytics.js','ga');ga('create', '" tracking "', 'auto');ga('send', 'pageview');"])]))
