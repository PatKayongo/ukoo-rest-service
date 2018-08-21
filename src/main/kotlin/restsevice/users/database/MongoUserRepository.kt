package restsevice.users.database

import org.springframework.data.mongodb.repository.MongoRepository

interface MongoUserRepository : MongoRepository<UserEntity, String> {
    fun findByEmail(email: String): UserEntity?
}