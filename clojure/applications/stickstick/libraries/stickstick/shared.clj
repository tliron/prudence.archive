;
; A shared namespace for Stickstick
;

(ns stickstick)

(import 'java.util.concurrent.locks.ReentrantLock)

(defonce connection-pool (atom nil))
(defonce connection-pool-lock (ReentrantLock.))
