package com.leakingcode.datatypes

class EitherSuspendMonad<T>(private val eitherBlock: suspend () -> Either<T>) {

    suspend fun <Q> bind(f: suspend (Right<T>) -> Either<Q>): EitherSuspendMonad<Q> {
        return when (val either = eitherBlock()) {
            is Right -> EitherSuspendMonad { f(either) }
            is Left -> EitherSuspendMonad { Left(either.error) }
        }
    }

    suspend fun unit(): Either<T> = eitherBlock()
}
