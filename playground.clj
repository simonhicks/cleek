(defmacro defword
  [name docstring effect & code]
  (let [args (vec (take-while (partial not= '--) effect))]
    `(def ~(with-meta name {:doc (str effect "\n  " docstring)})
       (with-meta (fn [stack#]
                    (apply (fn ~args ~@code)
                           (take-last ~(count args) stack#)))
                  {::word true ::stack-effect '~effect ::num-args ~(count args)}))))

(defn word?
  [item]
  (-> item meta ::word))

(defn apply-word
  "apply the given word to the stack"
  [stack word]
  (let [result (word stack)
        num-args (-> word meta ::num-args)]
    (vec (concat (drop-last num-args stack) result))))

(defn apply*
  "add the item to the stack... if it's a word apply it, else push the value onto the stack"
  [stack item]
  (if (word? item)
    (apply-word stack item)
    (conj stack item)))

(defn run
  "Apply the given words and or values"
  [& queue]
  (reduce apply* [] queue))

(defword dup
  "Duplicates the top item on the stack"
  [a -- a a]
  [a a])

(defword swap
  "Swaps the position of the top two items on the stack"
  [a b -- b a]
  [b a])

(run 1 2 3 swap dup)
