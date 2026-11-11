package com.bbr.service

import com.bbr.client.BananaClient
import com.bbr.dto.AnswerRequest
import com.bbr.dto.UserResponse
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import spock.lang.Specification

class PuzzleServiceSpec extends Specification {

    BananaClient banana = Mock()
    ProgressService progress = Mock()
    PuzzleService service = new PuzzleService(banana, new LevelService(), progress)

    def "a new puzzle sends the image and the time, never the solution"() {
        given:
        banana.fetch() >> new BananaClient.BananaPuzzle("http://img/1.png", 7)

        when:
        def p = service.newPuzzle(1L, 1)

        then:
        1 * progress.checkUnlocked(1L, 1)
        p.image() == "http://img/1.png"
        p.timeLimit() == 20
        p.getClass().getRecordComponents()*.name == ["puzzleId", "image", "timeLimit"]   // no solution field
    }

    def "a locked level cannot get a puzzle"() {
        given:
        progress.checkUnlocked(1L, 3) >> { throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Level locked") }

        when:
        service.newPuzzle(1L, 3)

        then:
        def e = thrown(ResponseStatusException)
        e.statusCode.value() == 403
        0 * banana.fetch()
    }

    def "the right answer in time gives the bonus and the server unlocks the next level"() {
        given:
        banana.fetch() >> new BananaClient.BananaPuzzle("img", 7)
        def p = service.newPuzzle(1L, 1)

        when:
        def r = service.answer(1L, new AnswerRequest(p.puzzleId(), 7))

        then:
        1 * progress.unlockNext(1L, 1) >> new UserResponse("player1", 2, 0)
        r.correct()
        !r.timeUp()
        r.bonus() == 200 * 1 + 20 * 20      // level 1: 200 x level + 20 x seconds left
        r.highestLevel() == 2
    }

    def "a wrong answer #answer does not unlock"() {
        given:
        banana.fetch() >> new BananaClient.BananaPuzzle("img", 7)
        progress.get(1L) >> new UserResponse("player1", 1, 0)
        def p = service.newPuzzle(1L, 1)

        when:
        def r = service.answer(1L, new AnswerRequest(p.puzzleId(), answer))

        then:
        !r.correct()
        r.bonus() == 0
        r.highestLevel() == 1
        0 * progress.unlockNext(_, _)

        where:
        answer << [0, 6, 8]
    }

    def "each puzzle can be answered only once"() {
        given:
        banana.fetch() >> new BananaClient.BananaPuzzle("img", 7)
        progress.unlockNext(1L, 1) >> new UserResponse("player1", 2, 0)
        def p = service.newPuzzle(1L, 1)
        service.answer(1L, new AnswerRequest(p.puzzleId(), 7))

        when:
        service.answer(1L, new AnswerRequest(p.puzzleId(), 7))

        then:
        def e = thrown(ResponseStatusException)
        e.statusCode.value() == 404
    }

    def "another player cannot answer my puzzle"() {
        given:
        banana.fetch() >> new BananaClient.BananaPuzzle("img", 7)
        def p = service.newPuzzle(1L, 1)

        when:
        service.answer(2L, new AnswerRequest(p.puzzleId(), 7))

        then:
        def e = thrown(ResponseStatusException)
        e.statusCode.value() == 404
        0 * progress.unlockNext(_, _)
    }

    def "if the Banana API is down the player gets 503"() {
        given:
        banana.fetch() >> { throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Puzzle service is busy, try again") }

        when:
        service.newPuzzle(1L, 1)

        then:
        def e = thrown(ResponseStatusException)
        e.statusCode.value() == 503
    }
}
