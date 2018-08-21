package users

import at.favre.lib.crypto.bcrypt.BCrypt
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.*
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import restsevice.users.User
import restsevice.users.UserRepository
import restsevice.users.UserResource
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

@DisplayName("User REST Resource")
class UserResourceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var mockMvc: MockMvc;

    @BeforeEach
    fun setup() {
        this.userRepository = mock(UserRepository::class.java)
        val userResource = UserResource(
                this.userRepository,
                Clock.fixed(Instant.parse("2019-01-03T10:15:30.00Z"), ZoneId.systemDefault()),
                "the-jwt-secret")
        this.mockMvc = MockMvcBuilders.standaloneSetup(userResource).build()
    }

    @Test
    fun `should save new user with hashed password to the database`() {
        this.mockMvc.perform(
                post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"example@ukoo.com\",\"password\":\"P@ssword1\"}"))
                .andExpect(status().isOk)

        val userCaptor = argumentCaptor<User>()
        verify(userRepository, times(1)).saveUser(userCaptor.capture())
        Assertions.assertEquals("example@ukoo.com", userCaptor.firstValue.email)
        Assertions.assertTrue(BCrypt.verifyer().verify("P@ssword1".toCharArray(), userCaptor.firstValue.password).verified)
    }

    @Test
    fun `should return a 400 if the user already exists in the database`() {
        val hashedPassword = BCrypt.withDefaults().hashToString(12, "P@ssword1".toCharArray())
        val savedUser = User("example@ukoo.com", hashedPassword)
        whenever(this.userRepository.findByEmail("example@ukoo.com")).thenReturn(savedUser)

        this.mockMvc.perform(
                post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"example@ukoo.com\",\"password\":\"P@ssword1\"}"))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.errors[0]").value("Username exists"))
    }

    @Test
    fun `should return jwt token when logging in with correct details`() {
        val hashedPassword = BCrypt.withDefaults().hashToString(12, "P@ssword1".toCharArray())
        val savedUser = User("example@ukoo.com", hashedPassword)
        whenever(this.userRepository.findByEmail("example@ukoo.com")).thenReturn(savedUser)

        this.mockMvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"example@ukoo.com\",\"password\":\"P@ssword1\"}"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.data.token").isNotEmpty)
    }

    @Test
    fun `should return a 401 error if the user does not exist when logging in`() {
        whenever(this.userRepository.findByEmail("example@ukoo.com")).thenReturn(null)
        this.mockMvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"example@ukoo.com\",\"password\":\"P@ssword1\"}"))
                .andExpect(status().isUnauthorized)
                .andExpect(jsonPath("$.errors[0]").value("Invalid username or password"))
    }

    @Test
    fun `should return a 401 http status if the password is wrong when logging in`() {
        val hashedPassword = BCrypt.withDefaults().hashToString(12, "P@ssword1".toCharArray())
        val savedUser = User("example@ukoo.com", hashedPassword)
        whenever(this.userRepository.findByEmail("example@ukoo.com")).thenReturn(savedUser)

        this.mockMvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"example@ukoo.com\",\"password\":\"Wr0NgP@ssw0rd!\"}"))
                .andExpect(status().isUnauthorized)
                .andExpect(jsonPath("$.errors[0]").value("Invalid username or password"))
    }
}