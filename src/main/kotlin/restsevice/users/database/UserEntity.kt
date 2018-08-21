package restsevice.users.database

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "users")
data class UserEntity(val email: String, val password: String, @Id val id: String? = null) {
}