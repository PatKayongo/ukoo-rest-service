package restsevice.users

import at.favre.lib.crypto.bcrypt.BCrypt
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import restsevice.common.JsonResponse
import java.time.Clock
import java.time.LocalDateTime
import java.util.*

@RestController
class UserResource(val userRepository: UserRepository, val clock: Clock, @Value("\${jwt.secret}") val jwtSecret: String) {

    @PostMapping("/user")
    public fun createUser(@RequestBody userCredentials: UserCredentials): ResponseEntity<JsonResponse<Nothing>> {
        val existingUser = userRepository.findByEmail(userCredentials.email)
        if (existingUser != null) {
            val unauthorizedResponseData = JsonResponse(null, listOf("Username exists"))
            return ResponseEntity(unauthorizedResponseData, HttpStatus.BAD_REQUEST)
        }

        val hashedPassword = BCrypt.withDefaults().hashToString(12, userCredentials.password.toCharArray())
        userRepository.saveUser(User(userCredentials.email, hashedPassword))
        return ResponseEntity(JsonResponse(null), HttpStatus.OK)
    }

    @PostMapping("/login")
    public fun login(@RequestBody userCredentials: UserCredentials) : ResponseEntity<JsonResponse<LoginResult>> {
        val unauthorizedResponseData = JsonResponse(null as? LoginResult, listOf("Invalid username or password"))
        val unauthorizedResponse = ResponseEntity(unauthorizedResponseData, HttpStatus.UNAUTHORIZED)

        val user = userRepository.findByEmail(userCredentials.email) ?: return unauthorizedResponse;
        val isPasswordValid = BCrypt.verifyer().verify(userCredentials.password.toCharArray(), user.password.toCharArray()).verified
        if (!isPasswordValid) {
            return unauthorizedResponse
        }

        val jwt = createJwt(userCredentials.email)
        return ResponseEntity(JsonResponse(LoginResult(jwt)), HttpStatus.OK);
    }

    private fun createJwt(userEmail: String): String {
        val currentTime = LocalDateTime.now(clock).atZone(clock.zone)
        val expiryTime = currentTime.plusHours(1);
        return Jwts.builder()
                .setIssuedAt(Date.from(currentTime.toInstant()))
                .setSubject(userEmail)
                .setExpiration(Date.from(expiryTime.toInstant()))
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact()
    }
}

data class UserCredentials(val email: String, val password: String)
data class LoginResult(val token: String)