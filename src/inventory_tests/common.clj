(ns inventory-tests.common
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.data.csv :as csv]
            [clj-http.client :as http]
            [cheshire.core :as json]
            [slingshot.slingshot :refer [try+]]))

(def base-listing-service-url "localhost:8080"
;"listingsvc001.useast2.rentpath.com"
)

(defn raw-locations
  [locations-uri]
  (with-open [r (io/reader locations-uri)] 
    (doall
     (csv/read-csv r))))

(defn csv->map
  [parsed-csv]
  (map zipmap
       (->> (first parsed-csv)
            
            (map keyword)
            repeat)
       (rest parsed-csv)))

(defn write-map-to-csv
  [map-data outfile]
  (with-open [w (io/writer outfile)]
    (let [header (map name (keys (first map-data)))
          data (map (comp vec vals) map-data)]
      (csv/write-csv w
                     (cons header data)))))

(defn dashify-string
  [s]
  (-> s
      str/trim
      (str/replace #"[.|'|)]" "")
      (str/replace  #" |[(|]" "-")))

(defn get-listings
  [base-url request]
  (try+
   (json/parse-string
    (:body (http/get request 
                     {:headers {"X-Application-ID" "ag/www"}}
                     {:accept :json})))
   (catch [:status 500]
    {:meta {:total "Err-500"}})))

(defn get-listings-total
  [listings-map]
  (get-in listings-map ["meta" "total"]))

(defn update-locations-with-search-test-counts
  [base-url locations-map search-tests]
  (map (fn [{:keys [city state] :as location}]
         (let [listings-by-search-test 
               (map (fn [search-test]
                      (let [listings (get-listings base-url city state search-test)
                            listings-count (get-listings-total listings)]
                        {(keyword search-test) listings-count}))
                    search-tests)]
           (into location (vec listings-by-search-test))))
       locations-map))
