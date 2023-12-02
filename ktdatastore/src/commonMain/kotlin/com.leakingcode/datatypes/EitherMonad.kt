package com.leakingcode.datatypes

class EitherMonad<T>(private val eitherBlock: suspend () -> Either<T>) {

    suspend fun <Q> bind(f: (Right<T>) -> Either<Q>): EitherMonad<Q> {
        return when (val either = eitherBlock()) {
            is Right -> EitherMonad { f(either) }
            is Left -> EitherMonad { Left(either.error) }
        }
    }

    suspend fun unit(): Either<T> = eitherBlock()
}
