package com.leakingcode.datatypes

sealed class Either<T> {

    fun justOrError(): T {
        return when (this) {
            is Right -> value
            is Left -> throw error
        }
    }

    fun just(): T? {
        return when (this) {
            is Right -> value
            is Left -> null
        }
    }

    fun <Q> map(f: (T) -> Q): Either<Q> {
        return when (this) {
            is Right -> Right(f(value))
            is Left -> Left(error)
        }
    }

    fun <T, Q> Either<T>.fMap(f: (T) -> Either<Q>): Either<Q> {
        return when (this) {
            is Right -> f(value)
            is Left -> Left(error)
        }
    }

    suspend fun <T, Q> Either<T>.mapSuspend(f: suspend (T) -> Q): Either<Q> {
        return when (this) {
            is Right -> Right(f(value))
            is Left -> Left(error)
        }
    }
}

data class Right<T>(val value: T) : Either<T>()
data class Left<T>(val error: Error) : Either<T>()
