package ru.job4j.utils;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.*;

class HabrCareerDateTimeParserTest {
    @Test
    void whenFormatCorrect() {
        HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
        String data = "2023-05-03T09:53:01+03:00";
        LocalDateTime result = parser.parse(data);
        assertThat(result).isEqualTo("2023-05-03T09:53:01");
    }
}