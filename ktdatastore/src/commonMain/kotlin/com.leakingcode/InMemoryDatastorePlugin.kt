package com.leakingcode

import com.leakingcode.datatypes.Left
import com.leakingcode.datatypes.Maybe
import com.leakingcode.datatypes.Right
import kotlin.random.Random

class InMemoryDatastorePlugin : PluginDatastore<PluginDatastore.GenericId> {

    private val inMemoryMap = HashMap<String, Map<String, Any>>()

    private var readingWriteFlag = true

    override fun put(
        entity: Map<String, Any>,
        entityName: String
    ): Maybe<PluginDatastore.GenericId> {
        if (readingWriteFlag) {
            generateKey().let {
                inMemoryMap[it] = entity
                return Right(PluginDatastore.GenericId(it))
            }
        } else {
            return Left(Error("Transaction"))
        }
    }

    override fun get(id: PluginDatastore.GenericId): Maybe<Map<String, Any>> {
        return try {
            Right(inMemoryMap[id.value]!!)
        } catch (nullPointer: NullPointerException) {
            Left(Error(nullPointer))
        }
    }

    override fun query(
        entityName: String,
        byPropertyValue: List<Pair<String, Any>>
    ): List<Pair<PluginDatastore.GenericId, Map<String, Any>>> {
        return inMemoryMap.toList()
            .filter {
                byPropertyValue.map { byPropertyValue: Pair<String, Any> ->
                    it.second[byPropertyValue.first] == byPropertyValue.second
                }.reduce { acc, b -> acc and b }
            }
            .map {
                Pair(PluginDatastore.GenericId(it.first), it.second)
            }
    }

    override fun count(entityName: String): Long {
        return inMemoryMap.count().toLong()
    }

    override fun delete(id: PluginDatastore.GenericId):
            Maybe<PluginDatastore.GenericId> {
        inMemoryMap.remove(id.value)?.let {
            return Right(id)
        }
        return Left(Error("Trying to remove, key ${id.value} not present."))
    }

    override fun transaction(blockingCall: () -> Maybe<out Any>) {
        try {
            blockingCall().justOrError()
        } catch (stateError: Error) {
            // nothing
        }
    }

    private fun generateKey() = Random.nextInt().toString()
}
