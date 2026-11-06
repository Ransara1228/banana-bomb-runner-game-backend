/*
 * AI-assisted: Claude (Anthropic), 2026-11-04.
 * Used for: first version of this file. Change it, test it and make sure you understand it before submitting.
 */
package com.bbr.service

import org.springframework.web.server.ResponseStatusException
import spock.lang.Specification

class LevelServiceSpec extends Specification {

    LevelService levels = new LevelService()

    def "level #id gives #seconds seconds for the gate puzzle"() {
        expect:
        levels.get(id).puzzleTime() == seconds

        where:
        id | seconds
        1  | 20
        2  | 15
        3  | 10
        4  | 8
        5  | 6
    }

    def "level #id does not exist"() {
        when:
        levels.get(id)

        then:
        thrown(ResponseStatusException)

        where:
        id << [0, 6, -1]
    }
}
