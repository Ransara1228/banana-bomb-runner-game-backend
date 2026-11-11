package com.bbr.service

import com.bbr.dto.LoginRequest
import com.bbr.dto.RegisterRequest
import com.bbr.entity.User
import com.bbr.repository.UserRepository
import com.bbr.security.JwtService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.server.ResponseStatusException
import spock.lang.Specification

import java.time.Instant

class AuthServiceSpec extends Specification {

    UserRepository users = Mock()
    JwtService jwt = Mock()
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4)   // low strength = fast tests
    AuthService service = new AuthService(users, encoder, jwt)

    def "register saves a BCrypt hash, not the password"() {
        given:
        users.existsByEmail("a@b.com") >> false
        User saved = null

        when:
        service.register(new RegisterRequest("a@b.com", "player1", "secret123"))

        then:
        1 * users.save(_ as User) >> { User u -> saved = u; u }

        and:
        saved.passwordHash != "secret123"
        encoder.matches("secret123", saved.passwordHash)
    }

    def "the same email cannot register twice"() {
        given:
        users.existsByEmail("a@b.com") >> true

        when:
        service.register(new RegisterRequest("a@b.com", "player1", "secret123"))

        then:
        def e = thrown(ResponseStatusException)
        e.statusCode.value() == 409
        0 * users.save(_)
    }

    def "the 5th wrong password locks the account for 5 minutes"() {
        given:
        def user = newUser("secret123")
        user.failedLogins = 4
        users.findByEmail("a@b.com") >> Optional.of(user)

        when:
        service.login(new LoginRequest("a@b.com", "wrong1"))

        then:
        def e = thrown(ResponseStatusException)
        e.statusCode.value() == 401
        user.lockedUntil != null
        user.lockedUntil.isAfter(Instant.now().plusSeconds(290))
        0 * jwt.create(_)
    }

    def "a locked account cannot log in even with the right password"() {
        given:
        def user = newUser("secret123")
        user.lockedUntil = Instant.now().plusSeconds(120)
        users.findByEmail("a@b.com") >> Optional.of(user)

        when:
        service.login(new LoginRequest("a@b.com", "secret123"))

        then:
        def e = thrown(ResponseStatusException)
        e.statusCode.value() == 429
        0 * jwt.create(_)
    }

    def "the right password clears the counter and gives a JWT"() {
        given:
        def user = newUser("secret123")
        user.failedLogins = 3
        users.findByEmail("a@b.com") >> Optional.of(user)

        when:
        def result = service.login(new LoginRequest("a@b.com", "secret123"))

        then:
        1 * jwt.create(user) >> "token-123"
        result.token() == "token-123"
        result.user().username() == "player1"
        user.failedLogins == 0
    }

    private User newUser(String password) {
        def u = new User()
        u.email = "a@b.com"
        u.username = "player1"
        u.passwordHash = encoder.encode(password)
        return u
    }
}
