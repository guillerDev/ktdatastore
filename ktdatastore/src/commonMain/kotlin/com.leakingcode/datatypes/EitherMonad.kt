package com.leakingcode.datatypes

class EitherMonad<T>(private val eitherBlock: () -> Either<T>) {

    fun <Q> bind(f: (Right<T>) -> Either<Q>): EitherMonad<Q> {
        return when (val either = eitherBlock()) {
            is Right -> EitherMonad { f(either) }
            is Left -> EitherMonad { Left(either.error) }
        }
    }

    fun unit(): Either<T> = eitherBlock()
}
