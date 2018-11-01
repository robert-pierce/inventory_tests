(ns inventory-tests.common
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.data.csv :as csv]
            [clj-http.client :as http]
            [cheshire.core :as json]))

(def base-url "localhost:8080")

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

(defn whitespace-to-dash
  [s]
  (-> s
      (str/replace #"[.]" "")
      (str/replace  #" |[(]" "-")))

(defn listing-request
  [listing-srvc-base-url city state search-test]
  (let [formated-city (whitespace-to-dash city)
        formated-state (whitespace-to-dash state)]
    (format "http://%s/v2/listings?criteria[citySlug]=%s&criteria[stateSlug]=%s&searchTest=%s"
            listing-srvc-base-url formated-city formated-state search-test)))

(defn get-listings
  [base-url city state search-test]
  (json/parse-string 
   (:body(http/get (listing-request base-url city state search-test) 
             {:headers {"X-Application-ID" "ag/www"}}
             {:accept :json}))))

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

(defn process-search-tests
  [raw-locations-uri search-tests outfile-uri]
  (let [read-locations (raw-locations raw-locations-uri)
        locations-map (csv->map read-locations)
        processed-results (update-locations-with-search-test-counts base-url locations-map search-tests)]
    (write-map-to-csv processed-results outfile-uri)))
