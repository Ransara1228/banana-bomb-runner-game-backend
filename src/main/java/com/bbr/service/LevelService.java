package com.bbr.service;

import com.bbr.dto.LevelDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class LevelService {

    /** One place for the level rules. The frontend gets them as JSON. */
    private static final List<LevelDto> LEVELS = List.of(
            new LevelDto(1, "Jungle Trail", 1.0, 20, 500, 3, "level1.svg"),
            new LevelDto(2, "River Bend", 1.3, 15, 700, 4, "level2.svg"),
            new LevelDto(3, "Temple Ruins", 1.6, 10, 900, 5, "level3.svg"),
            new LevelDto(4, "Volcano Pass", 2.0, 8, 1100, 6, "level4.svg"),
            new LevelDto(5, "Bomb Factory", 2.5, 6, 1300, 7, "level5.svg"));

    public List<LevelDto> all() {
        return LEVELS;
    }

    public LevelDto get(int id) {
        if (id < 1 || id > LEVELS.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No such level");
        }
        return LEVELS.get(id - 1);
    }

    public int count() {
        return LEVELS.size();
    }
}
