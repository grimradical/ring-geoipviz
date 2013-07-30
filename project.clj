(defproject grimradical/ring-geoipviz "0.2.0-SNAPSHOT"
  :description "Ring middleware for computing and visualizing geoip information for clients"
  :url "http://github.com/grimradical/ring-geoipviz"
  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.txt"
            :distribution :repo}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "[1.3.0,)"]
                 [cheshire "4.0.1"]
                 [geocoder-clj "0.0.6"]
                 [ring/ring-core "[1.1.3,)"]]
)
