package job.grabber.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class HabrCareerDateTimeParserTest {

    @Test
    public void dateTimeTest() {
        HabrCareerDateTimeParser dateTimeParser = new HabrCareerDateTimeParser();
        String in  = "2023-05-03T20:27:04+03:00";
        LocalDateTime ref = LocalDateTime.parse("2023-05-03T20:27:04");
        assertThat(dateTimeParser.parse(in)).isEqualTo(ref);
    }
}