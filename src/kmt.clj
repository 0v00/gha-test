#!/usr/bin/env bb

(require '[cheshire.core :as json])
(require '[babashka.http-client :as http])

(defn get-pr-id [args]
  (if (nil? (first args))
    (throw (RuntimeException. "You must provide a PR ID."))
    (first args)))

(def gh-token (str "Bearer " (System/getenv "TEST_SECRET")))
(def REPO "gha-test")
(def OWNER "0v00")
(def BASE-URL "https://api.github.com")
(def PR-ID (get-pr-id *command-line-args*))

;; Construct https://api.github.com/repos/<OWNER>/<REPO>/pulls/pr-id/files
(defn pr-files-url [base-url owner repo pr-id]
  (str base-url "/repos/" owner "/" repo "/pulls/" pr-id "/files"))

;; Returns changed files in a PR
(def pr-files-resp (http/get (pr-files-url BASE-URL OWNER REPO PR-ID)
                             {:headers {:Accept "application/vnd.github+json",
                                        :Authorization gh-token,
                                        :X-GitHub-Api-Version "2022-11-28"}}))

(def changed-pr-files (json/parse-string (:body pr-files-resp)))

;; The same as doing: (def filenames (mapv (fn [file] (get file "filename")) changed-pr-files))
(def filenames
  (mapv #(get % "filename")
        changed-pr-files))

(println filenames)

;; check if empty just in case

;; (def gh-token (System/getenv "TEST_TOKEN"))