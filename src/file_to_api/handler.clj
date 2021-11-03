(ns file-to-api.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [clojure.string :as str]
            [file-to-api.json :as json]))


(defroutes app-routes
  (GET "/" [] "Hello there")
  (context "/json" [] 
    (route/not-found json/handler))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
