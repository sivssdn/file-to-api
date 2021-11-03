(ns file-to-api.json
  (:require [clojure.string :as str]
            [clojure.data.json :as jsn]))

;filters json maps which contains path keys
(defn filterKeys [path records]
  (def objKeys (map #(keyword %) path))
  (def nestedObj (map #(get-in % (vec objKeys)) records))
  (filter some? nestedObj)) ;removing nils

;returns boolean true if all keys of query is present in map object
(defn mapContains? [query mapObj]
  (defn isEveryKeyPresent? [qry obj] (if (every? obj (keys qry)) 
    true 
    false))
  (if (instance? clojure.lang.PersistentArrayMap mapObj) 
    (isEveryKeyPresent? query mapObj) 
    false))

;runs query on the filtered records
(defn filterquery [query records] 
  (def newQuery (map #(hash-map (keyword (key %)) (val %)) query)) ;mapping keys from ab to :ab
  (def queryMap (into {} newQuery)) ;from ({} {}) to {}
  (def queryKeys (map #(keyword %) (keys queryMap))) ;contains query keys [:a :b]
  (def recordsWithMatchingKeys (filter #(mapContains? queryMap %) records))
  (def recordsWithMatchingValues (filter #(= queryMap (select-keys % (vec queryKeys))) recordsWithMatchingKeys))
  recordsWithMatchingValues)

(defn fetch [path query] 
  (def all-records (jsn/read-str (slurp "/Users/paras/Documents/dev/code/clojure/file-to-api/data.json")
                :key-fn keyword))
  (def records (if (instance? clojure.lang.PersistentVector all-records) all-records (first all-records)))
  (ns-unmap 'file-to-api.json 'all-records) ;freeing memory from original json
  (def filteredRecords (filterKeys (drop 2 path) records))
  (def result (filterquery query filteredRecords))
  (vec result)
  )

(defn handler [& args]
  (def argMap (first args))
  (def path (str/split (argMap :uri) #"/"))
  (def query (argMap :query-params))
  (def result (fetch path query))
  (jsn/write-str result))
