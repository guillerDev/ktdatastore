package com.leakingcode

import com.leakingcode.datatypes.Maybe

interface PluginDatastore<I> {

    fun put(entity: Map<String, Any>, entityName: String): Maybe<I>

    fun update(
        id: I,
        entity: Map<String, Any>,
    ): Maybe<Unit>

    fun get(id: I): Maybe<Map<String, Any>>

    fun query(
        entityName: String,
        byPropertyValue: List<Pair<String, Any>>
    ): List<Pair<I, Map<String, Any>>>

    fun count(entityName: String): Long

    fun delete(id: I): Maybe<I>

    fun transaction(blockingCall: () -> Maybe<out Any>)

    class GenericId(
        val value: String,
    ) : Any()
}
