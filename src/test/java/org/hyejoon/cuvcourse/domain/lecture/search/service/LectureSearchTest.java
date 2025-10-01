package org.hyejoon.cuvcourse.domain.lecture.search.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.hyejoon.cuvcourse.domain.lecture.repository.LectureJpaRepository;
import org.hyejoon.cuvcourse.support.Performance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
public class LectureSearchTest {

    @Autowired
    private LectureJpaRepository lectureJpaRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=0");
        jdbcTemplate.execute("TRUNCATE TABLE lectures");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=1");
    }

    @Test
    void fulltext_vs_like() {
        // Given
        String KW = "최적화";
        double HIT_RATIO = 0.0001;   // 키워드 포함 비율
        int total = 1_000_000;

        int HIT = insertShuffled(total, HIT_RATIO, KW);
        System.out.println("HIT: " + HIT);

        // 통계 최신화
        jdbcTemplate.execute("ANALYZE TABLE lectures");

        Pageable page = PageRequest.of(2, 10);

        // When
        // FULLTEXT
        String booleanQ = "+" + KW;
        Page<Lecture> ftBoolean = lectureJpaRepository.searchTitleBoolean(booleanQ, page);
        Page<Lecture> ftNatural = lectureJpaRepository.searchTitleNatural(KW, page);

        // LIKE
        Page<Lecture> lk = lectureJpaRepository.searchTitleLike(KW, page);

        // Then
        assertThat(ftBoolean.getTotalElements()).isEqualTo(HIT);
        assertThat(ftNatural.getTotalElements()).isEqualTo(HIT);
        assertThat(lk.getTotalElements()).isEqualTo(HIT);

        assertThat(ftBoolean.getContent()).allSatisfy(l ->
            assertThat(l.getLectureTitle()).contains(KW));
        assertThat(ftNatural.getContent()).allSatisfy(l ->
            assertThat(l.getLectureTitle()).contains(KW));
        assertThat(lk.getContent()).allSatisfy(l ->
            assertThat(l.getLectureTitle()).contains(KW));

        // 시간 비교 (FT-BOOLEAN, FT-NATURAL, LIKE)
        long[] ftBooleanStats = Performance.samplesMs(2, 5,
            () -> lectureJpaRepository.searchTitleBoolean(booleanQ, page));
        long[] ftNaturalStats = Performance.samplesMs(2, 5,
            () -> lectureJpaRepository.searchTitleNatural(KW, page));
        long[] lkStats = Performance.samplesMs(2, 5,
            () -> lectureJpaRepository.searchTitleLike(KW, page));

        System.out.println(
            Performance.toMarkdown("FT(BOOLEAN)", ftBooleanStats, "FT(NATURAL)", ftNaturalStats,
                "LIKE", lkStats));

    }

    int insertShuffled(int total, double hitRatio, String kw) {
        final String sql = """
              INSERT INTO lectures (id, lecture_title, professor_name, credits, capacity, total, created_at)
              VALUES (?, ?, ?, ?, ?, ?, NOW())
            """;
        int BATCH = 5000;
        int includeKw = 0;
        long id = 1;
        var rnd = new java.util.Random(7); // 고정 seed

        List<Object[]> buf = new ArrayList<>(BATCH);
        for (int i = 0; i < total; i++, id++) {
            boolean hit = rnd.nextDouble() < hitRatio; // 무작위 분포
            includeKw += hit ? 1 : 0;
            String title = hit ? ("검색 " + kw + " 적용 " + id) : ("동시성 제어 " + id);
            buf.add(new Object[]{id, title, "홍길동", 3, 30, 0});
            if (buf.size() == BATCH) {
                jdbcTemplate.batchUpdate(sql, buf);
                buf.clear();
            }
        }
        if (!buf.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, buf);
        }

        return includeKw;
    }

}
