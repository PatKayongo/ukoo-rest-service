package restsevice.users.database

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import restsevice.users.User

@DisplayName("Database User Repository Tests")
@DataMongoTest
@ExtendWith(SpringExtension::class)
class DatabaseUserRepositoryTest {
    private lateinit var databaseUserRepository: DatabaseUserRepository
    private lateinit var mongoUserRepository: MongoUserRepository

    @BeforeEach
    fun setup(@Autowired databaseUserRepository: DatabaseUserRepository, @Autowired mongoUserRepository: MongoUserRepository) {
        this.mongoUserRepository = mongoUserRepository
        this.databaseUserRepository = databaseUserRepository
        this.mongoUserRepository.deleteAll()
    }

    @Test
    fun `should return null if user doesn't exist when searching by email`() {
        val existingUser = this.databaseUserRepository.findByEmail("existing-email@ukoo.africa")
        Assertions.assertNull(existingUser)
    }

    @Test
    fun `should return saved user when searching by email`() {
        this.mongoUserRepository.save(UserEntity("existing-email@ukoo.africa", "hashed-password"))
        val existingUser = this.databaseUserRepository.findByEmail("existing-email@ukoo.africa")

        Assertions.assertNotNull(existingUser)
        Assertions.assertEquals("existing-email@ukoo.africa", existingUser?.email)
        Assertions.assertEquals("hashed-password", existingUser?.password)
    }

    @Test
    fun `should save specified user into the database`() {
        databaseUserRepository.saveUser(User("email@ukoo.afica", "hashed-password"))
        val allUsers = this.mongoUserRepository.findAll()
        Assertions.assertEquals(1, allUsers.size)
        Assertions.assertNotNull(allUsers[0].id)
        Assertions.assertEquals("email@ukoo.afica", allUsers[0].email)
        Assertions.assertEquals("hashed-password", allUsers[0].password)
    }

    @Test
    fun `when saving it should update details of existing user`() {
        databaseUserRepository.saveUser(User("email@ukoo.afica", "hashed-password"))
        databaseUserRepository.saveUser(User("email@ukoo.afica", "new-hashed-password"))

        val allUsers = this.mongoUserRepository.findAll()
        Assertions.assertEquals(1, allUsers.size)
        Assertions.assertEquals("email@ukoo.afica", allUsers[0].email)
        Assertions.assertEquals("new-hashed-password", allUsers[0].password)
    }
}