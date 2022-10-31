import kotlinx.serialization.Serializable

@Serializable
data class AddressEntity(
    val address: String,
    val zipCode: Long = 0L
)
