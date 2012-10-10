(ns ring-geoipviz.core
  (:require [ring.util.response :as rr]
            [cheshire.core :as json])
  (:use [geocoder.core :only (geocode-ip-address)]))

(defn wrap-with-geoip
  "Ring middleware that adds a :geoip key to the current request. The
  value is a map containing location information about the client.

  The 'ks' argument is a list of keys used to get at the IP address
  for the client. [:remote-addr] is sensible for 99% of installations,
  or you can supply your own keys like [:headers \"x-real-ip\"] if
  there a different key containing the correct client IP."
  [app ks]
  (fn [req]
    (let [geoip (first (geocode-ip-address (get-in req ks)))]
      (app (assoc req :geoip geoip)))))

(defn wrap-with-buffer
  "Ring middleware that aggregates data in a format suitable for
  polling clients.

  Values are extracted from each request and appended to an internal
  buffer along with the current timestamp. Clients can request the
  items in the buffer that are more recent than a timestamp they
  supply. The buffer is bounded, and uses a FIFO eviction policy.

  When a particular URL is requested by a client (the URL itself is
  configurable), the client will get back the contents of the buffer
  matching their request. The result format is JSON of the form:

    {\"tstamp\": <number of millis since epoch>,
     \"items\": [<json serialized version of extracted data>
                 ...
                 ...]}

  f: function that takes the request object as an argument, and
  returns a value for the request. That value will be appended to the
  internal buffer, and that clients may retrieve.

  url: what URL clients should hit to retrieve items from the
  buffer. If the request :uri matches this, then JSON results are
  returned to the client. Note that this URL will shadow any other
  routing...the wrapped ring application isn't called when a matching
  URL is found.

  howmany: the maximum size of the buffer, or essentially how much
  history to keep.

  Example: allow clients to get a time-series view into the :geoip
  request attribute. Clients can hit /geo-poll to get JSON results,
  and we'll store a max of 100 entries.

    (wrap-with-long-poller :geoip \"/geo-poll\" 100)
  "
  [app f url howmany]
  (let [items (atom [])]
    (fn [{:keys [params] :as req}]
      (cond
       (= (req :uri) (str url "/viz"))
       (rr/resource-response "ring_geoipviz/index.html")

       (= (req :uri) (str url "/map-coords"))
       (rr/resource-response "ring_geoipviz/world-paths.json")

       ;; data requested
       (= (req :uri) (str url "/buffer"))
       (let [now (System/currentTimeMillis)
             seen (Long/parseLong (get params "seen" (str now)))
             result (for [[tstamp item] @items
                          :when (> tstamp seen)]
                      item)]
         (-> {:tstamp now
              :items result}
             (json/generate-string)
             (rr/response)
             (rr/header "Access-Control-Allow-Origin" "*")))

       ;; vanilla request
       :else
       (do
         (when-let [item (f req)]
           (swap! items conj [(System/currentTimeMillis) item])
           (when (> (count @items) howmany)
             (swap! items #(into [] (rest %)))))
         (app req))))))

