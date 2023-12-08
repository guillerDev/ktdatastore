package com.leakingcode.datatypes

/**
 * At binding handle if Left case from eitherBlock, otherwise Right case from block @param eitherBlock
 * It is useful for switching Right when there is Left case in @param eitherBlock
 */
class LeftMonad<T>(
    private val eitherBlock: suspend () -> Either<T>
) {
    suspend fun bind(f: suspend (Left<T>) -> Either<T>): LeftMonad<T> {
        return when (val either = eitherBlock()) {
            is Right -> LeftMonad { either }
            is Left -> LeftMonad { f(either) }
        }
    }

    suspend fun unit(): Either<T> = eitherBlock()
}


suspend fun main() {
    val monad = LeftMonad<String> {
        Left(Error("error monad"))
    }.bind { it: Left<String> ->
        Right(it.error.message ?: "")
    }

    println(monad.unit().justOrError())
}
