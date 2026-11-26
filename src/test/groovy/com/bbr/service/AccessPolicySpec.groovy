/*
 * AI-assisted: Claude (Anthropic), 2026-11-24.
 * Used for: first version of this file. Change it, test it and make sure you understand it before submitting.
 */
package com.bbr.service

import org.springframework.web.server.ResponseStatusException
import spock.lang.Specification

class AccessPolicySpec extends Specification {

    ProgressService progress = Mock()
    AccessPolicy access = new AccessPolicy(progress)

    def "only levels 1 and 2 are free"() {
        expect:
        AccessPolicy.isFree(level) == free

        where:
        level | free
        1     | true
        2     | true
        3     | false
        5     | false
    }

    def "a guest can play free level #level"() {
        when:
        access.checkCanPlay(null, level)

        then:
        noExceptionThrown()
        0 * progress._                      // a guest has no progress to check

        where:
        level << [1, 2]
    }

    def "a guest must log in to play level #level"() {
        when:
        access.checkCanPlay(null, level)

        then:
        def e = thrown(ResponseStatusException)
        e.statusCode.value() == 401
        e.reason == "Log in to play level " + level
        0 * progress._

        where:
        level << [3, 4, 5]
    }

    def "a logged-in player is checked against their own progress"() {
        when:
        access.checkCanPlay(7L, 4)

        then:
        1 * progress.checkUnlocked(7L, 4)
    }
}
