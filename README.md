# clonk

A Clojure dsl for concatenative programming. The idea was inspired by Factjor and is very heavily influenced by the Factor language. Having said that, there are some pretty major conceptual differences which are intended to solve some of the irritations I felt when using Factor for anything more than trivial.

## Usage

Expressions are created by passing a sequence of values to the interpreter. These values can either be data (eg. numbers, lists, strings, etc.) or "words" (which are equivalent to functions). These values are stored in a stack and whenever a word is added, it is called using values from the top of the stack as arguments and pushing it's returns back onto the stack. For example:

    (run 1 2 3) ; there are no words here, so this just yields a stack of numbers
    ; => [1 2 3]

    (run 1 2 3 +) ; + will act on the top two values and it's result is added back to the stack
    ; => [1 5]

Words can be defined using clojure code with `defprimitive`

    (defprimitive +
      "adds two values together"
      [int1 int2 -- sum]
      [(clojure.core/+ int1 int2)])

The vector of symbols after the docstring (the one that looks like an arg list) is called the stack effect. The symbols before `--` are the args. These are pulled from the top of the stack and unlike Factor or Factjor, they are available in the body of the definition as locals. The symbols after `--` represent how many values will be pushed back onto the stack.

Primitives should always return a vector, containing the items to be pushed onto the stack.

Words can also be defined by composing other words and values with `defword` like so:

    (defword double [int1 -- int2] 2 * )

This means that `(run 5 double)` is identical to `(run 5 2 *)`

Again, the vector of symbols is the stack effect, and the symbols before the `--` are available as locals in the word definition like so:

    (defword dup
      "Duplicate the top value on the stack"
      [a -- a a]
      a)


Words defined with `defword` are not required to return vectors. The reason for this, is that *they are not executing clojure code*. They are compositions of values that will be added to the stack as if they had been typed out manually.


You can also use vectors as 'quotations' or anonymous words. These are called using the `call` word, so the following two expressions are equivalent.

    (run 1 2 +)

    (run 1 [2 +] call)

## License

Copyright © 2013 Simon Hicks

Distributed under the Eclipse Public License, the same as Clojure.
