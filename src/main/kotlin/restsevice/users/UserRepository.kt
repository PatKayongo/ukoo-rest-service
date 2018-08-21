package restsevice.users

interface UserRepository {
    fun saveUser(user: User)
    fun findByEmail(email: String): User?
}