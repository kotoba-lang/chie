(ns chie.test-runner
  (:require [clojure.java.io :as io]
            [clojure.test :as test]))

(defn- file-ns [file]
  (with-open [r (java.io.PushbackReader. (io/reader file))]
    (second (read {:read-cond :allow :features #{:clj}} r))))

(defn -main [& _]
  (let [files (->> (file-seq (io/file "test/chie"))
                   (filter #(.isFile %))
                   (filter #(re-find #"\.clj(c)?$" (.getName %)))
                   (remove #(= "test_runner.clj" (.getName %)))
                   (sort-by str))
        namespaces (mapv file-ns files)]
    (doseq [file files] (load-file (str file)))
    (let [{:keys [fail error]} (apply test/run-tests namespaces)]
      (shutdown-agents)
      (when (pos? (+ fail error)) (System/exit 1)))))
