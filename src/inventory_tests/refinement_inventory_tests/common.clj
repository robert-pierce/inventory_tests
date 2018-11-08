(ns inventory-tests.refinement-inventory-tests.common
  (:require [inventory-tests.common :as common]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.data.csv :as csv]
            [clj-http.client :as http]
            [cheshire.core :as json]
            [slingshot.slingshot :refer [try+]]))

(def bedroom-refinements {:studio "criteria[bedroomRange][high]=0&criteria[bedroomRange][low]=0"
                          :one-bedroom "criteria[bedroomRange][high]=1&criteria[bedroomRange][low]=1"
                          :two-bedroom "criteria[bedroomRange][high]=2&criteria[bedroomRange][low]=2"})

(def refinement-queries (merge {} bedroom-refinements))

(defn listing-request
  [listing-srvc-base-url city state refinement]
  (let [formated-city (common/dashify-string city)
        formated-state (common/dashify-string state)]
    (if (= :base refinement)
      (format "http://%s/v2/listings?criteria[citySlug]=%s&criteria[stateSlug]=%s"
              listing-srvc-base-url formated-city formated-state)
      (if-let [refinement-query (refinement refinement-queries)]
        (format "http://%s/v2/listings?criteria[citySlug]=%s&criteria[stateSlug]=%s&%s"
                listing-srvc-base-url formated-city formated-state refinement-query)))))

(defn update-locations-with-search-test-counts
  [base-url locations-map refinements]
  (map (fn [{:keys [city state] :as location}]
         (let [listings-by-refinement 
               (map (fn [refinement]
                      (let [request (listing-request base-url city state refinement)
                            listings (common/get-listings request)
                            listings-count (common/get-listings-total listings)]
                        {(keyword refinement) listings-count}))
                    refinements)]
           (into location (vec listings-by-refinement))))
       locations-map))

(defn process-refinement-search-tests
  [raw-locations-uri refinements outfile-uri]
  (let [read-locations (common/raw-locations raw-locations-uri)
        locations-map (common/csv->map read-locations)
        augmented-search-tests (into [:base] refinements)
        processed-results (update-locations-with-search-test-counts 
                           common/base-listing-service-url 
                           locations-map augmented-search-tests)]
    (common/write-map-to-csv processed-results outfile-uri)))
