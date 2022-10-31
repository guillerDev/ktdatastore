package com.leakingcode

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.properties.Properties
import kotlinx.serialization.properties.decodeFromMap
import kotlinx.serialization.properties.encodeToMap

class KtDatastore<I>(
    val plugin: PluginDatastore<I>
) {

    @OptIn(ExperimentalSerializationApi::class)
    inline fun <reified T> store(entity: T): Maybe<I> {
        return plugin.put(
            Properties.encodeToMap(entity),
            entity!!::class.simpleName ?: "" // todo
        )
    }

    @OptIn(ExperimentalSerializationApi::class)
    inline fun <reified T> get(id: I): Maybe<T> {
        return plugin.get(id).map {
            Properties.decodeFromMap(it)
        }
    }

    fun remove(id: I): Maybe<I> {
        return plugin.delete(id)
    }

    fun transaction(blockingCall: () -> Maybe<out Any>) {
        plugin.transaction(blockingCall)
    }

    @OptIn(ExperimentalSerializationApi::class)
    inline fun <reified T> query(byPropertyValue: List<Pair<String, Any>> = emptyList()): List<Pair<I, T>> {
        return plugin.query(T::class.simpleName ?: "", byPropertyValue).map {
            Pair(it.first, Properties.decodeFromMap(it.second))
        }
    }

    inline fun <reified T> count() {
        plugin.counta(T::class.simpleName ?: "")
    }
}
