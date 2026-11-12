package com.bbr.service

import com.bbr.entity.OtpCode
import com.bbr.event.OtpRequestedEvent
import com.bbr.repository.OtpRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.server.ResponseStatusException
import spock.lang.Specification

import java.time.Instant

class OtpServiceSpec extends Specification {

    OtpRepository otpRepo = Mock()
    ApplicationEventPublisher events = Mock()
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4)   // low strength = fast tests
    OtpService service = new OtpService(otpRepo, encoder, events)

    def "sendCode saves only the hash and publishes an event"() {
        given:
        otpRepo.findTopByEmailOrderByCreatedAtDesc("a@b.com") >> Optional.empty()
        OtpRequestedEvent event = null
        OtpCode saved = null

        when:
        service.sendCode("a@b.com")

        then:
        1 * otpRepo.save(_ as OtpCode) >> { OtpCode o -> saved = o; o }
        // typed parameter: Spock gives us the event itself (with Object it would give the whole argument list)
        1 * events.publishEvent(_ as OtpRequestedEvent) >> { OtpRequestedEvent e -> event = e }

        and:
        event.code() ==~ /\d{6}/                       // 6 digits
        saved.codeHash != event.code()                  // the code itself is not saved
        encoder.matches(event.code(), saved.codeHash)
    }

    def "a new code needs a 60 second wait"() {
        given:
        otpRepo.findTopByEmailOrderByCreatedAtDesc("a@b.com") >> Optional.of(newOtp("123456", Instant.now().plusSeconds(300)))

        when:
        service.sendCode("a@b.com")

        then:
        def e = thrown(ResponseStatusException)
        e.statusCode.value() == 429
        0 * events.publishEvent(_)
    }

    def "wrong code #code adds one attempt"() {
        given:
        def otp = newOtp("123456", Instant.now().plusSeconds(300))
        otpRepo.findTopByEmailOrderByCreatedAtDesc("a@b.com") >> Optional.of(otp)

        expect:
        !service.verify("a@b.com", code)
        otp.attempts == 1

        where:
        code << ["000000", "123457", "654321"]
    }

    def "the 6th try is blocked even with the right code"() {
        given:
        def otp = newOtp("123456", Instant.now().plusSeconds(300))
        otp.attempts = 5
        otpRepo.findTopByEmailOrderByCreatedAtDesc("a@b.com") >> Optional.of(otp)

        when:
        service.verify("a@b.com", "123456")

        then:
        thrown(ResponseStatusException)
        1 * otpRepo.deleteByEmail("a@b.com")
    }

    def "an expired code is rejected"() {
        given:
        otpRepo.findTopByEmailOrderByCreatedAtDesc("a@b.com") >> Optional.of(newOtp("123456", Instant.now().minusSeconds(1)))

        when:
        service.verify("a@b.com", "123456")

        then:
        def e = thrown(ResponseStatusException)
        e.reason.contains("expired")
    }

    def "the right code works once and is deleted"() {
        given:
        otpRepo.findTopByEmailOrderByCreatedAtDesc("a@b.com") >> Optional.of(newOtp("123456", Instant.now().plusSeconds(300)))

        when:
        boolean ok = service.verify("a@b.com", "123456")

        then:
        ok
        1 * otpRepo.deleteByEmail("a@b.com")
    }

    private OtpCode newOtp(String code, Instant expiresAt) {
        def o = new OtpCode()
        o.email = "a@b.com"
        o.codeHash = encoder.encode(code)
        o.createdAt = Instant.now()
        o.expiresAt = expiresAt
        return o
    }
}
