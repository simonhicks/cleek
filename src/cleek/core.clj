(ns cleek.core
  (:refer-clojure :exclude [+ - / * comp drop]))

; TODO
; - add stack effect checking for defword
; - build up std lib

(defn word?
  [item]
  (-> item meta ::word))

(defn impl
  [word]
  (-> word meta ::impl))

(defn- process-def-args
  [args]
  (let [[docstring effect & code] (if (string? (first args)) args (cons (str) args))]
    (into [docstring effect] code)))

(defn- get-stack-args
  [stack-effect]
  (take-while (partial not= '--) stack-effect))

(defn apply*
  "add the item to the stack... if it's a word apply it, else push the value onto the stack"
  [stack item]
  (if (word? item)
    ((impl item) stack)
    (conj stack item)))

(defmacro defspecial
  "Define a special word based on a function that takes a stack and returns a modified stack"
  [name & more]
  (let [[docstring effect function] (process-def-args more)
        args (get-stack-args effect)]
    `(def ~(with-meta name {:doc (str effect "\n  " docstring)})
       (with-meta '~name
                  {::impl ~function ::word true ::stack-effect '~effect}))))

(defmacro defprimitive
  [name & more]
  (let [[docstring effect & code] (process-def-args more)
        args (vec (get-stack-args effect))]
    `(defspecial ~name ~docstring ~effect
       (fn [stack#]
         (let [result# (apply (fn ~args ~@code) (take-last ~(count args) stack#))]
           (vec (concat (drop-last ~(count args) stack#) result#)))))))

(defmacro defword
  [name & more]
  (let [[docstring effect & code] (process-def-args more)
        args (vec (get-stack-args effect))]
    `(defspecial ~name ~docstring ~effect
       (fn [stack#]
         (apply (fn ~args (reduce apply* stack# (list ~@code))) (take-last ~(count args) stack#))))))

(defn run
  "Apply the given words and or values"
  [& queue]
  (reduce apply* [] queue))

(defword dup
  "Duplicates the top item on the stack"
  [a -- a a] a)

(defprimitive swap
  "Swaps the position of the top two items on the stack"
  [a b -- b a]
  [b a])

(defprimitive + [int1 int2 -- sum]
  [(clojure.core/+ int1 int2)])
(defprimitive - [int1 int2 -- sum]
  [(clojure.core/- int1 int2)])
(defprimitive / [int1 int2 -- sum]
  [(clojure.core// int1 int2)])
(defprimitive * [int1 int2 -- sum]
  [(clojure.core/* int1 int2)])

(defspecial rotate+ [--]
  (fn [stack] (vec (cons (last stack) (drop-last stack)))))
(defspecial rotate- [--]
  (fn [stack] (conj (vec (rest stack)) (first stack))))

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
      (if (word? callable)
        (apply* remaining callable)
        (reduce apply* remaining callable)))))

(defprimitive drop [item --] [])

(defword dip
  "This calls a callable with the first item underneath it removed, and then adds that
  item back again afterwards"
  [item callable -- item]
  swap drop call item)


(run 1 2 3 [+] dip)
