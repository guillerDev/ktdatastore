package com.leakingcode

import com.leakingcode.datatypes.Either

interface PluginDatastore<I> {

    fun put(entity: Map<String, Any>, entityName: String): Either<I>

    fun update(
        id: I,
        entity: Map<String, Any>,
    ): Either<Unit>

    fun get(id: I): Either<Map<String, Any>>

    fun query(
        entityName: String,
        byPropertyValue: List<Pair<String, Any>>
    ): List<Pair<I, Map<String, Any>>>

    fun count(entityName: String): Long

    fun delete(id: I): Either<I>

    fun transaction(blockingCall: () -> Either<out Any>)

    class GenericId(
        val value: String,
    ) : Any()
}
