(ns cleek.words
  (:use [cleek.core :only (defspecial defprimitive defword)])
  (:refer-clojure :exclude [+ - * / drop = concat inc dec]))

;(def if +)

;(apply (if true if -) [1 2 3])

; if is a special form, so there isn't actually a clojure.core/if var...
; we can happily redefine 'if' and as long as we don't expect to use it as a regular clojure function it'll work fine!

; TODO
; - build up std lib

(defword dup
  "Duplicates the top item on the stack"
  [a -- a a] a)

(defprimitive swap
  "Swaps the position of the top two items on the stack"
  [a b -- b a]
  [b a])

(defprimitive +
  "Adds the top two items together"
  [int1 int2 -- sum]
  [(clojure.core/+ int1 int2)])

(defprimitive -
  "Subtracts the top item from the second from top item"
  [int1 int2 -- i1-i2]
  [(clojure.core/- int1 int2)])

(defprimitive /
  "Divide the top two items by each other"
  [int1 int2 -- i1/i2]
  [(clojure.core// int1 int2)])

(defprimitive *
  "Multiply the top two items on the stack"
  [int1 int2 -- product]
  [(clojure.core/* int1 int2)])

(defspecial call
  "Calls the top callable on the stack. Typically, this is used to 
  apply all the items in a vector in sequence, so this:

    (run 1 1 +)

  is equivalent to this:

    (run 1 [1 +] call)
  "
  [callable --]
  (fn [stack]
    (let [callable (last stack)
          remaining (vec (drop-last stack))]
      (if (cleek.core/word? callable)
        (cleek.core/apply* remaining callable)
        (reduce cleek.core/apply* remaining callable)))))

(defprimitive drop
  "Drop the top item from the stack"
  [item --]
  [])

(defword dip
  "This calls a callable with the first item underneath it removed, and then adds that
  item back again afterwards"
  [item callable -- item]
  swap drop call item)

(defprimitive .
  "Print the top value on the stack and drop it"
  [item --]
  (prn item)
  [])

(def t [drop call])
(def f [swap drop call])

(defword if
  [then else ? --]
  call call)

(defprimitive =
  "checks if the top two items are the same"
  [i1 i2 -- ?]
  [(if (clojure.core/= i1 i2) t f)])

(defprimitive <vector>
  "return a new, empty vector"
  [-- v]
  [[]])

(defprimitive append
  "append a value onto a collection"
  [coll item -- coll]
  [(into coll [item])])

(defword prepend
  [item coll --] 
  [<vector> swap append] dip concat)

(defprimitive concat
  "append the items from coll2 to coll1"
  [coll1 coll2 -- coll]
  [(into coll1 coll2)])


(use '[cleek.core :only (run)])

;(run ["True"] ["False"] [1 2 =] if)

;(run 1 2 3 4 5 [+] 4 times)

(defword dec [n -- n] 1 -)
(defword inc [n -- n] 1 +)

(defword dup2 [a b -- a b a b] a b)
(defword dip2 [callable --] <vector> swap append [dip] concat dip)

(defprimitive <word> [s -- w] [(eval (symbol s))])

(defword ^:private if-zero [then else n --] swap [swap] dip [0 =] prepend if)

(comment FIXME This is broken
(defword times [callable n --] dup2 [[] if-zero] dip2 dec); "times" <word> call drop drop)

(run 1 2 3 4 5 [+] 1 dup2 dec [[drop call] dip call] [] if-zero)
)

