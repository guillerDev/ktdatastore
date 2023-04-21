package com.leakingcode

import com.google.cloud.datastore.testing.RemoteDatastoreHelper
import com.leakingcode.datatypes.Left
import com.leakingcode.datatypes.Either
import com.leakingcode.datatypes.Right
import com.leakingcode.datatypes.EitherMonad
import java.util.Random
import kotlin.math.absoluteValue
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.test.fail
import org.junit.After

class KtDatastoreTest {


//    private val remoteDatastoreHelper = RemoteDatastoreHelper.create()

    private fun providePlugin() =
        listOf(
            InMemoryDatastorePlugin(),
//            GcloudPlugin(remoteDatastoreHelper.options.service)
        )

    @After
    fun tearDown() {
//        remoteDatastoreHelper.deleteNamespace()
    }

    @Test
    fun testStore() {
        val addressToStore = AddressEntity("St. John Pink. 69.")
        providePlugin().forEach {
            KtDatastore(it).apply {
                when (val either = store(addressToStore)) {
                    is Right ->
                        assertTrue {
                            addressToStore.address
                                .contentEquals(get<AddressEntity>(either.value).justOrError().address)
                        }

                    is Left -> fail(either.error.message)
                }
            }
        }
    }

    @Test
    fun testQueryByProperty() {
        val addressToStore = AddressEntity("St. John Pink. 69.", 999)
        providePlugin().forEach {
            KtDatastore(it).apply {
                EitherMonad { store(addressToStore) }
                    .bind {
                        Right(query(AddressEntity("St. John Pink. 69.", 999)))
                    }
                    .bind { right ->
                        assertEquals(1, right.value.size, plugin.toString())
                        Right(Unit)
                    }
                    .unit()
                    .justOrError()
            }
        }
    }

    @Test
    fun testStoreGetNonExisting() {
        val addressToStore = AddressEntity("St. John Pink. 69.")
        providePlugin().forEach {
            KtDatastore(it).apply {
                assertFailsWith<Error> {
                    EitherMonad { store(addressToStore) }
                        .bind {
                            get<AddressEntity>(it.value)
                        }
                        .bind {
                            get<AddressEntity>(PluginDatastore.GenericId("non-existing"))
                        }.unit()
                        .justOrError()
                }
            }
        }
    }

    @Test
    fun testStoreAndRemove() {
        val addressToStore = AddressEntity("St. John Pink. 69.")
        providePlugin().forEach {
            KtDatastore(it).apply {
                assertFailsWith<Error> {
                    EitherMonad { store(addressToStore) }
                        .bind { right ->
                            get<AddressEntity>(right.value)
                                .map { right.value }
                        }.bind {
                            remove(it.value)
                        }.bind { right ->
                            // Try to get removed entity, it should not be possible if previous step worked.
                            get<AddressEntity>(right.value)
                                .map { right.value }
                        }.bind {
                            fail("This should not be reached, since previous step failed.")
                            get<AddressEntity>(it.value)
                        }.unit()
                        .justOrError()
                }
            }
        }
    }

    @Test
    fun testQueryParam() {
        val addressToStore = AddressEntity(
            Random().ints(Random().nextLong().absoluteValue).toString(),
            Random().nextLong()
        )
        providePlugin().forEach { plugin ->
            KtDatastore(plugin).apply {
                EitherMonad { store(addressToStore) }
                    .bind {
                        val result: Either<List<Pair<PluginDatastore.GenericId, AddressEntity>>> =
                            Right(
                                query(
                                    listOf(
                                        Pair("address", addressToStore.address),
                                        Pair("zipCode", addressToStore.zipCode)
                                    )
                                )
                            )
                        result
                    }
                    .unit()
                    .justOrError().let {
                        assertEquals(1, it.size, plugin.toString())
                    }
            }
        }
    }

    @Test
    fun testCount() {
        val addressToStore = AddressEntity("St. John Pink. 69.")
        providePlugin().forEach {
            KtDatastore(it)
                .apply {
                    val expectedSize = query<AddressEntity>().size
                    count<AddressEntity>()

                }
        }
    }

    @Test
    fun testTransactionFailsStoreAndRemove() {
        val addressToStore = AddressEntity("St. John Pink. 69.")
        providePlugin().forEach {
            KtDatastore(it)
                .apply {
                    val expectedSize = query<AddressEntity>().size
                    transaction {
                        EitherMonad { store(addressToStore) }
                            .bind { right ->
                                get<AddressEntity>(right.value)
                                    .map { right.value }
                            }.bind {
                                remove(it.value)
                            }.bind { right ->
                                get<AddressEntity>(right.value)
                                    .map { right.value }
                            }.bind {
                                fail()
                                remove(it.value)
                            }.unit()
                    }
                    assertEquals(expectedSize, query<AddressEntity>().size)
                }
        }
    }
}
