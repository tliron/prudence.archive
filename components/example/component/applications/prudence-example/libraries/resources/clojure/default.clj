
(require 'resources.clojure.person)

; Note that deftypes must be explicitly imported (they are true JVM classes) 
(import 'resources.clojure.person.Person)

(def resources {
  "person" (Person.)})
