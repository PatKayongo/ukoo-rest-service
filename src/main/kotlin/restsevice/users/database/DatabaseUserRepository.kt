package restsevice.users.database

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import restsevice.users.User
import restsevice.users.UserRepository

@Repository
class DatabaseUserRepository(val mongoUserRepository: MongoUserRepository) : UserRepository {
    override fun findByEmail(email: String): User? {
        val existingUser = this.mongoUserRepository.findByEmail(email) ?: return null;
        return User(existingUser.email, existingUser.password)
    }

    override fun saveUser(user: User) {
        val existingUserEntity = this.mongoUserRepository.findByEmail(user.email)
        this.mongoUserRepository.save(UserEntity(user.email, user.password, existingUserEntity?.id))
    }
}

@Document(collection = "users")
data class UserEntity(val email: String, val password: String, @Id val id: String? = null) {
}

interface MongoUserRepository : MongoRepository<UserEntity, String> {
    fun findByEmail(email: String): UserEntity?
}