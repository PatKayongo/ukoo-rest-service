package restsevice.users.database

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