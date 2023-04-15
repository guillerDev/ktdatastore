package com.leakingcode.datatypes

sealed class Maybe<T> {

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

    fun <Q> map(f: (T) -> Q): Maybe<Q> {
        return when (this) {
            is Right -> Right(f(value))
            is Left -> Left(error)
        }
    }
}

data class Right<T>(val value: T) : Maybe<T>()
data class Left<T>(val error: Error) : Maybe<T>()
