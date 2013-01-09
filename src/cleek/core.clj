(ns cleek.core)

; TODO
; - add stack effect checking for defword

(defn- impl
  "Return a words implementation function (or nil if word isn't a word)"
  [word]
  (-> word meta ::impl))

(defn word?
  "Return true if item is a word, false otherwise"
  [item]
  ((comp not nil? impl) item))

(defn- process-def-args
  "standardise the args passed to one of the def___ macros. Returns a vector containing
   the docstring, the stack-effect and the code."
  [args]
  (let [[docstring effect & code] (if (string? (first args)) args (cons (str) args))]
    (into [docstring effect] code)))

(defn- get-stack-args
  "Returns the list of symbols representing the args in the given stack effect"
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
                  {::impl ~function ::stack-effect '~effect}))))

(defmacro defprimitive
  "Define a word using clojure forms for the implementation"
  [name & more]
  (let [[docstring effect & code] (process-def-args more)
        args (vec (get-stack-args effect))]
    `(defspecial ~name ~docstring ~effect
       (fn [stack#]
         (let [result# (apply (fn ~args ~@code) (take-last ~(count args) stack#))]
           (vec (concat (drop-last ~(count args) stack#) result#)))))))

(defmacro defword
  "Define a word by composing other words"
  [name & more]
  (let [[docstring effect & code] (process-def-args more)
        args (vec (get-stack-args effect))]
    `(defspecial ~name ~docstring ~effect
       (fn [stack#]
         (apply (fn ~args (reduce apply* stack# (list ~@code))) (take-last ~(count args) stack#))))))

(defn run
  "Execute the given words and or values"
  [& queue]
  (reduce apply* [] queue))
