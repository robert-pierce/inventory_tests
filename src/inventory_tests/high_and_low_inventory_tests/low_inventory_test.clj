(ns inventory-tests.high-and-low-inventory-tests.low-inventory-test
  (:require [inventory-tests.high-and-low-inventory-tests.common :as common]))

(def search-tests ["low_inv_a" "low_inv_b" "low_inv_c" "low_inv_d"])
(def low-inv-locations-uri "low_inventory_cities_for_script_sample.csv")
(def low-inv-test-results-outfile "low_inventory_search_test_sample_results.csv")

(defn process-low-inv-location-tests
  [] 
  (common/process-search-tests low-inv-locations-uri 
                               search-tests 
                               low-inv-test-results-outfile)
  (println "Low Inventory Tests Completed"))
