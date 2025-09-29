package org.hyejoon.cuvcourse.domain.course.create.controller;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.persistence.EntityManager;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.hyejoon.cuvcourse.domain.course.create.service.CourseCreateService;
import org.hyejoon.cuvcourse.domain.course.create.service.CourseCreateServiceWithOptimistic;
import org.hyejoon.cuvcourse.domain.course.create.service.CourseCreateServiceWithPessimistic;
import org.hyejoon.cuvcourse.domain.course.create.service.CourseCreateServiceWithRedis;
import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.hyejoon.cuvcourse.domain.lecture.repository.LectureJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/sql/setup_concurrency.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/cleanup_concurrency.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class CreateConcurrencyControlTest {

    @Autowired
    CourseCreateService courseCreateService;
    @Autowired
    LectureJpaRepository lectureRepository;
    @Autowired
    CourseCreateServiceWithRedis courseCreateServiceWithRedis;
    @Autowired
    CourseCreateServiceWithOptimistic courseCreateServiceWithOptimistic;
    @Autowired
    CourseCreateServiceWithPessimistic courseCreateServiceWithPessimistic;
    @Autowired
    EntityManager em;

    private static void await(CyclicBarrier b) {
        try {
            b.await();
        } catch (Exception ignored) {
        }
    }

    @Test
    @DisplayName("락 없이 30명 동시 신청 시 정원을 초과한다")
    void 동시_수강신청_테스트_Without_Lock() throws Exception {
        Long lectureId = 1001L;
        int threads = 30;

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CyclicBarrier start = new CyclicBarrier(threads);
        CountDownLatch done = new CountDownLatch(threads);
        AtomicInteger success = new AtomicInteger();
        for (int i = 0; i < threads; i++) {
            final long studentId = 2001L + i; // SQL문
            pool.submit(() -> {
                await(start);
                try {
                    courseCreateService.createCourse(studentId, lectureId); // 락 없음
                    success.incrementAndGet();
                } catch (Exception ignore) {
                } finally {
                    done.countDown();
                }
            });
        }
        done.await();

        Lecture lecture = lectureRepository.findById(lectureId).orElseThrow();
        long enrolledCount = countEnrolled(lectureId);
        // 동시성 실패 기대
        assertTrue(success.get() > lecture.getCapacity() || enrolledCount > lecture.getCapacity());
    }

    @Test
    @DisplayName("Redis 분산락 적용 시 동시 30명에서 동시성 제어에 성공한다")
    void 동시_수강신청_테스트_With_RedisLock() throws Exception {
        Long lectureId = 1001L;
        int threads = 30;

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CyclicBarrier start = new CyclicBarrier(threads);
        CountDownLatch done = new CountDownLatch(threads);
        AtomicInteger success = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            final long studentId = 2001L + i; // SQL문
            pool.submit(() -> {
                await(start);
                try {
                    courseCreateServiceWithRedis.createCourseWithLock(studentId, lectureId);
                    success.incrementAndGet();
                } catch (Exception ignore) {
                } finally {
                    done.countDown();
                }
            });
        }
        done.await();

        Lecture lecture = lectureRepository.findById(lectureId).orElseThrow();
        long enrolledCount = countEnrolled(lectureId);

        // 동시성 성공 기대
        assertEquals(lecture.getCapacity(), enrolledCount);
        assertTrue(success.get() <= lecture.getCapacity());
    }

    @Test
    @DisplayName("비관적 락 적용 시 동시 30명에서 동시성 제어에 성공한다")
    void 동시_수강신청_테스트_With_PessimisticLock() throws Exception {
        Long lectureId = 1001L;
        int threads = 30;

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CyclicBarrier start = new CyclicBarrier(threads);
        CountDownLatch done = new CountDownLatch(threads);
        AtomicInteger success = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            final long studentId = 2001L + i; // SQL문
            pool.submit(() -> {
                await(start);
                try {
                    courseCreateServiceWithPessimistic.createCourseWithLock(studentId,
                        lectureId);
                    success.incrementAndGet();
                } catch (Exception ignore) {
                } finally {
                    done.countDown();
                }
            });
        }
        done.await();

        Lecture lecture = lectureRepository.findById(lectureId).orElseThrow();
        long enrolledCount = countEnrolled(lectureId);

        // 동시성 성공 기대
        assertEquals(lecture.getCapacity(), enrolledCount);
        assertTrue(success.get() <= lecture.getCapacity());
    }

    @Test
    @DisplayName("낙관적 락 적용 시 동시 30명에서 동시성 제어에 성공한다")
    void 동시_수강신청_테스트_With_OptimisticLock() throws Exception {
        Long lectureId = 1001L;
        int threads = 30;

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CyclicBarrier start = new CyclicBarrier(threads);
        CountDownLatch done = new CountDownLatch(threads);
        AtomicInteger success = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            final long studentId = 2001L + i; // SQL문
            pool.submit(() -> {
                await(start);
                try {
                    courseCreateServiceWithOptimistic.createCourseWithLock(studentId,
                        lectureId);
                    success.incrementAndGet();
                } catch (Exception ignore) {
                } finally {
                    done.countDown();
                }
            });
        }
        done.await();

        Lecture lecture = lectureRepository.findById(lectureId).orElseThrow();
        long enrolledCount = countEnrolled(lectureId);

        // 동시성 성공 기대
        assertEquals(lecture.getCapacity(), enrolledCount);
        assertTrue(success.get() <= lecture.getCapacity());
    }

    private long countEnrolled(Long lectureId) {
        Object single = em.createNativeQuery(
                "SELECT COUNT(*) FROM courses WHERE lecture_id = ?")
            .setParameter(1, lectureId)
            .getSingleResult();
        return ((Number) single).longValue();
    }
}
