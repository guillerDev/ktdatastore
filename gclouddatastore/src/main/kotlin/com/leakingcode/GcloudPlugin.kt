package com.leakingcode

import com.google.cloud.NoCredentials
import com.google.cloud.ServiceOptions
import com.google.cloud.datastore.AggregationQuery
import com.google.cloud.datastore.BooleanValue
import com.google.cloud.datastore.Datastore
import com.google.cloud.datastore.DatastoreException
import com.google.cloud.datastore.DatastoreOptions
import com.google.cloud.datastore.DoubleValue
import com.google.cloud.datastore.Entity
import com.google.cloud.datastore.Key
import com.google.cloud.datastore.KeyFactory
import com.google.cloud.datastore.LatLngValue
import com.google.cloud.datastore.ListValue
import com.google.cloud.datastore.LongValue
import com.google.cloud.datastore.Query
import com.google.cloud.datastore.QueryResults
import com.google.cloud.datastore.StringValue
import com.google.cloud.datastore.StructuredQuery
import com.google.cloud.datastore.StructuredQuery.PropertyFilter
import com.google.cloud.datastore.TimestampValue
import com.google.cloud.datastore.ValueType
import com.google.cloud.datastore.aggregation.Aggregation.count
import com.leakingcode.datatypes.Either
import com.leakingcode.datatypes.Left
import com.leakingcode.datatypes.Right

class GcloudPlugin(
    private val datastore: Datastore,
    private val namespace: String? = null
) : PluginDatastore<PluginDatastore.GenericId> {

    override fun put(
        entity: Map<String, Any>,
        entityName: String
    ): Either<PluginDatastore.GenericId> {
        return Right(
            PluginDatastore.GenericId(
                datastore.put(entity.entityBuilder(Entity.newBuilder(generateNewKey(entityName))))
                    .key.toUrlSafe()
            )
        )
    }

    override fun update(
        id: PluginDatastore.GenericId,
        entity: Map<String, Any>,
    ): Either<Unit> {
        return Right(datastore.update(entity.entityBuilder(Entity.newBuilder(Key.fromUrlSafe(id.value)))))
    }

    override fun get(id: PluginDatastore.GenericId): Either<Map<String, Any>> {
        return try {
            Right(datastore.get(Key.fromUrlSafe(id.value)).properties.mapGcloudValues())
        } catch (argumentError: IllegalArgumentException) {
            Left(Error(argumentError))
        } catch (datastoreError: DatastoreException) {
            Left(Error(datastoreError))
        } catch (nullPointer: NullPointerException) {
            Left(Error(nullPointer))
        }
    }

    override fun query(
        entityName: String,
        byPropertyValue: List<Pair<String, Any>>
    ): List<Pair<PluginDatastore.GenericId, Map<String, Any>>> {

        val filter = byPropertyValue.map {
            it.second.mapToPropertyFilter(it.first)
        }.reduceOrNull { a, b ->
            StructuredQuery.CompositeFilter.and(a, b) as StructuredQuery.Filter
        }

        val query: Query<Entity> = Query.newEntityQueryBuilder()
            .setNamespace(namespace)
            .setKind(entityName)
            .setFilter(filter)
            .build()
        val results: QueryResults<Entity> = datastore.run(query)
        return results.asSequence().map {
            Pair(PluginDatastore.GenericId(it.key.toUrlSafe()), it.properties.mapGcloudValues())
        }.toList()
    }

    override fun delete(id: PluginDatastore.GenericId): Either<PluginDatastore.GenericId> {
        return try {
            datastore.delete(Key.fromUrlSafe(id.value))
            Right(id)
        } catch (argumentError: IllegalArgumentException) {
            Left(Error("DataStore error at deleting $id", argumentError))
        } catch (dbError: DatastoreException) {
            Left(Error("DataStore error at deleting $id", dbError))
        }
    }

    override fun transaction(blockingCall: () -> Either<out Any>) {
        datastore.newTransaction().apply {
            try {
                blockingCall().justOrError()
                commit()
            } catch (transactionError: DatastoreException) {
                rollback()
            } catch (error: Error) {
                rollback()
            }
        }
    }

    override fun count(entityName: String): Long {
        val selectAllQuery = Query.newEntityQueryBuilder()
            .setNamespace(namespace)
            .setKind(entityName).build()
        val aggregationQuery: AggregationQuery =
            Query.newAggregationQueryBuilder()
                .setNamespace(namespace)
                .addAggregation(count().`as`(ALIAS_COUNT))
                .over(selectAllQuery).build()
        return datastore.runAggregation(aggregationQuery).first().get(ALIAS_COUNT)
    }

    private fun generateNewKey(entityName: String): Key {
        val keyFactory: KeyFactory = datastore
            .newKeyFactory().apply {
                setKind(entityName)
                if (namespace != null) setNamespace(namespace)
            }

        // Allocated id to verify that auto generated key does not exit already.
        return datastore.allocateId(keyFactory.newKey())
    }

    private fun Any.mapToPropertyFilter(property: String): StructuredQuery.Filter {
        return when (this) {
            is Long -> PropertyFilter.eq(property, this)
            is String -> PropertyFilter.eq(property, this)
            is Double -> PropertyFilter.eq(property, this)
            is Boolean -> PropertyFilter.eq(property, this)
            else -> throw IllegalStateException("Not supported.")
        }
    }

    private fun Map<String, com.google.cloud.datastore.Value<*>>.mapGcloudValues() =
        this.mapValues {
            when (it.value.type) {
                ValueType.NULL -> "null"
                ValueType.STRING -> (it.value as StringValue).get()
                ValueType.ENTITY -> TODO()
                ValueType.LIST -> (it.value as ListValue).get().map {
                }

                ValueType.KEY -> TODO()
                ValueType.LONG -> (it.value as LongValue).get()
                ValueType.DOUBLE -> (it.value as DoubleValue).get()
                ValueType.BOOLEAN -> (it.value as BooleanValue).get()
                ValueType.TIMESTAMP -> (it.value as TimestampValue).get()
                ValueType.BLOB -> TODO()
                ValueType.RAW_VALUE -> TODO()
                ValueType.LAT_LNG -> (it.value as LatLngValue).get()
                else -> "null"
            }
        }

    private fun Map<String, Any>.entityBuilder(builder: Entity.Builder): Entity {
        asIterable().forEach {
            // Map type to datastore type
            when (val value = it.value) {
                is String -> builder.set(it.key, value.StringValue())
                is Long -> builder.set(it.key, value)
                is Boolean -> builder.set(it.key, value)
                is Double -> builder.set(it.key, value)
                // todo scope more types, blob or latlong
                // todo scope to update
                else -> throw IllegalStateException("${value.javaClass} not supported.")
            }
        }
        return builder.build()
    }

    private fun String.StringValue(): StringValue? {
        return StringValue.of(this).toBuilder()
            .apply {
                if (this@StringValue.length > 1500) excludeFromIndexes = true
            }
            .build()
    }

    companion object {

        /**
         * gcloud beta emulators datastore start
         */
        fun buildEmulatedDatastore(): Datastore {
            val options = DatastoreOptions.newBuilder()
                .setProjectId(DatastoreOptions.getDefaultProjectId())
                .setHost("localhost:8081")
                .setCredentials(NoCredentials.getInstance())
                .setRetrySettings(ServiceOptions.getNoRetrySettings())
                .build()
            return options.service
        }

        private const val ALIAS_COUNT = "total_count"
    }
}
