import com.leakingcode.GcloudPlugin
import com.leakingcode.InMemoryDatastorePlugin
import com.leakingcode.KtDatastore
import com.leakingcode.Left
import com.leakingcode.Maybe
import com.leakingcode.PluginDatastore
import com.leakingcode.Right
import com.leakingcode.datatypes.MaybeMonad
import java.util.Random
import kotlin.math.absoluteValue
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.test.fail

class KtDatastoreTest {

    private fun providePlugin() = listOf(InMemoryDatastorePlugin(), GcloudPlugin())

    @Test
    fun testStore() {
        val addressToStore = AddressEntity("St. John Pink. 69.")
        providePlugin().forEach {
            KtDatastore(it).apply {
                when (val maybe = store(addressToStore)) {
                    is Right ->
                        assertTrue {
                            addressToStore.address
                                .contentEquals(get<AddressEntity>(maybe.value).justOrError().address)
                        }

                    is Left -> fail(maybe.error.message)
                }
            }
        }
    }

    @Test
    fun testStoreGetNonExisting() {
        val addressToStore = AddressEntity("St. John Pink. 69.")
        providePlugin().forEach {
            KtDatastore(it).apply {
                assertFailsWith<IllegalStateException> {
                    MaybeMonad { store(addressToStore) }
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
                assertFailsWith<IllegalStateException> {
                    MaybeMonad { store(addressToStore) }
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
    fun testQuery() {
        val addressToStore = AddressEntity(
            Random().ints(Random().nextLong().absoluteValue).toString(),
            Random().nextLong()
        )
        providePlugin().forEach { plugin ->
            KtDatastore(plugin).apply {
                MaybeMonad { store(addressToStore) }
                    .bind {
                        val result: Maybe<List<Pair<PluginDatastore.GenericId, AddressEntity>>> =
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
                        MaybeMonad { store(addressToStore) }
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
