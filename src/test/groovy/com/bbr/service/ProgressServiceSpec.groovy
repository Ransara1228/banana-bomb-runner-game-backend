package com.bbr.service

import com.bbr.entity.User
import com.bbr.event.LevelUnlockedEvent
import com.bbr.repository.UserRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.web.server.ResponseStatusException
import spock.lang.Specification

class ProgressServiceSpec extends Specification {

    UserRepository users = Mock()
    ApplicationEventPublisher events = Mock()
    ProgressService service = new ProgressService(users, new LevelService(), events)

    def "finishing my highest level unlocks the next one and publishes an event"() {
        given:
        def u = user(1)
        users.findById(1L) >> Optional.of(u)

        when:
        def r = service.unlockNext(1L, 1)

        then:
        r.highestLevel() == 2
        1 * users.save(u)
        1 * events.publishEvent({ it instanceof LevelUnlockedEvent && it.newLevel() == 2 })
    }

    def "replaying an old level changes nothing"() {
        given:
        users.findById(1L) >> Optional.of(user(3))

        when:
        def r = service.unlockNext(1L, 1)

        then:
        r.highestLevel() == 3
        0 * users.save(_)
        0 * events.publishEvent(_)
    }

    def "level 5 is the last one"() {
        given:
        users.findById(1L) >> Optional.of(user(5))

        when:
        def r = service.unlockNext(1L, 5)

        then:
        r.highestLevel() == 5
        0 * events.publishEvent(_)
    }

    def "a locked level is refused with 403"() {
        given:
        users.findById(1L) >> Optional.of(user(1))

        when:
        service.checkUnlocked(1L, 3)

        then:
        def e = thrown(ResponseStatusException)
        e.statusCode.value() == 403
    }

    private static User user(int highestLevel) {
        def u = new User()
        u.username = "player1"
        u.highestLevel = highestLevel
        return u
    }
}
