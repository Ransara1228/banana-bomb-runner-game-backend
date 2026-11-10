package com.bbr.service

import com.bbr.dto.ScoreRequest
import com.bbr.entity.Score
import com.bbr.entity.User
import com.bbr.repository.ScoreRepository
import com.bbr.repository.UserRepository
import spock.lang.Specification

class ScoreServiceSpec extends Specification {

    ScoreRepository scores = Mock()
    UserRepository users = Mock()
    ScoreService service = new ScoreService(scores, users)

    def "a better score becomes the new best score"() {
        given:
        def u = user("player1", 500)
        users.findById(1L) >> Optional.of(u)

        when:
        def r = service.save(1L, new ScoreRequest(1, 640))

        then:
        1 * scores.save(_ as Score)
        1 * users.save(u)
        r.bestScore() == 640
    }

    def "a lower score is saved but the best score stays"() {
        given:
        def u = user("player1", 900)
        users.findById(1L) >> Optional.of(u)

        when:
        def r = service.save(1L, new ScoreRequest(2, 300))

        then:
        1 * scores.save(_ as Score)
        0 * users.save(_)
        r.bestScore() == 900
    }

    def "the leaderboard shows each player once with their best score"() {
        given:
        users.findTop10ByBestScoreGreaterThanOrderByBestScoreDesc(0) >> [user("ransara", 910), user("nimal", 640)]

        when:
        def board = service.leaderboard()

        then:
        board*.username == ["ransara", "nimal"]
        board*.points == [910, 640]
    }

    private static User user(String name, int best) {
        def u = new User()
        u.username = name
        u.bestScore = best
        u.highestLevel = 2
        return u
    }
}
