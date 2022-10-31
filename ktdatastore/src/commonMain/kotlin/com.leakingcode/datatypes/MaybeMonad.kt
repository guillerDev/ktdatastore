package com.leakingcode.datatypes

import com.leakingcode.Left
import com.leakingcode.Maybe
import com.leakingcode.Right

class MaybeMonad<T>(private val maybeBlock: () -> Maybe<T>) {

    fun <Q> bind(f: (Right<T>) -> Maybe<Q>): MaybeMonad<Q> {
        return when (val maybe = maybeBlock()) {
            is Right -> MaybeMonad { f(maybe) }
            is Left -> MaybeMonad { Left(maybe.error) }
        }
    }

    fun unit(): Maybe<T> = maybeBlock()
}
