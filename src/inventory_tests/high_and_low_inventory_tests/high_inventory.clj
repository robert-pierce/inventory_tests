(ns inventory-tests.high-and-low-inventory-tests.high-inventory
  (:require [inventory-tests.high-and-low-inventory-tests.common :as common]))

(def search-tests ["hi_inv_a" "hi_inv_b"])
(def high-inv-locations-uri "high_inventory_cities_for_script.csv")
(def high-inv-test-results-outfile "high_inventory_search_test_results.csv")

(defn process-high-inv-location-tests
  [] 
  (common/process-search-tests high-inv-locations-uri 
                               search-tests 
                               high-inv-test-results-outfile)
  (println "High Inventory Tests Completed"))
