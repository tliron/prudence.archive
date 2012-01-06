;
; A shared namespace for Stickstick.
;
; Note the important of using defonce for all vars!
;

(ns stickstick.shared)

(import 'java.util.concurrent.locks.ReentrantLock)

(defonce connection-pool (atom nil)) ; We don't have a use for an Atom's thread safety here, but Clojure doesn't have a thread-unsafe equivalent!
(defonce connection-pool-lock (ReentrantLock.))
