(ns inventory-tests.high-and-low-inventory-tests.common
  (:require [inventory-tests.common :as common]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.data.csv :as csv]
            [clj-http.client :as http]
            [cheshire.core :as json]
            [slingshot.slingshot :refer [try+]]))

(defn listing-request
  [listing-srvc-base-url city state search-test]
  (let [formated-city (common/dashify-string city)
        formated-state (common/dashify-string state)]
    (if (= :base search-test)
      (format "http://%s/v2/listings?criteria[citySlug]=%s&criteria[stateSlug]=%s"
              listing-srvc-base-url formated-city formated-state)
      (format "http://%s/v2/listings?criteria[citySlug]=%s&criteria[stateSlug]=%s&searchTest=%s"
              listing-srvc-base-url formated-city formated-state search-test))))

(defn update-locations-with-search-test-counts
  [base-url locations-map search-tests]
  (map (fn [{:keys [city state] :as location}]
         (let [listings-by-search-test 
               (map (fn [search-test]
                      (let [request (listing-request base-url city state search-test)
                            listings (common/get-listings request)
                            listings-count (common/get-listings-total listings)]
                        {(keyword search-test) listings-count}))
                    search-tests)]
           (into location (vec listings-by-search-test))))
       locations-map))

(defn process-search-tests
  [raw-locations-uri search-tests outfile-uri]
  (let [read-locations (common/raw-locations raw-locations-uri)
        locations-map (common/csv->map read-locations)
        augmented-search-tests (into [:base] search-tests)
        processed-results (update-locations-with-search-test-counts common/base-listing-service-url 
                                                                    locations-map augmented-search-tests)]
    (common/write-map-to-csv processed-results outfile-uri)))
