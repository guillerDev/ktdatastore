package com.leakingcode

interface PluginDatastore<I> {

    fun put(entity: Map<String, Any>, entityName: String): Maybe<I>

    fun get(id: I): Maybe<Map<String, Any>>

    fun query(
        entityName: String,
        byPropertyValue: List<Pair<String, Any>>
    ): List<Pair<I, Map<String, Any>>>

    fun counta(entityName: String): Long

    fun delete(id: I): Maybe<I>

    fun transaction(blockingCall: () -> Maybe<out Any>)

    class GenericId(
        val value: String,
    ) : Any()
}
