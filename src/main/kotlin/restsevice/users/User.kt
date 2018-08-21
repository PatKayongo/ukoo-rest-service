package restsevice.users

import org.springframework.data.annotation.Id

class User(val email: String, val password: String) {
}