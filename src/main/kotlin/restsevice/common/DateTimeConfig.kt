package restsevice.common

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
class DateTimeConfig {

    @Bean
    fun getClock(): Clock {
        return Clock.systemUTC()
    }
}