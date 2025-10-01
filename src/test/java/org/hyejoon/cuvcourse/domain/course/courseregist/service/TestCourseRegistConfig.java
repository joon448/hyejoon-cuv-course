package org.hyejoon.cuvcourse.domain.course.courseregist.service;

import org.hyejoon.cuvcourse.domain.course.courseregist.seat.SeatGate;
import org.hyejoon.cuvcourse.domain.course.repository.CourseJpaRepository;
import org.hyejoon.cuvcourse.domain.lecture.repository.LectureJpaRepository;
import org.hyejoon.cuvcourse.domain.student.repository.StudentJpaRepository;
import org.hyejoon.cuvcourse.global.lock.DistributedLock;
import org.hyejoon.cuvcourse.global.lock.LockManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestCourseRegistConfig {

    @Bean
    @Qualifier("courseRegistRedissonService")
    public CourseRegistService redissonCourseRegistService(
        CourseCreationService courseCreationService,
        LockManager lockManager,
        @Qualifier("redissonLock") DistributedLock redissonLock,
        CourseJpaRepository courseJpaRepository,
        LectureJpaRepository lectureJpaRepository,
        StudentJpaRepository studentJpaRepository) {
        return new CourseRegistService(courseCreationService, lockManager, redissonLock,
            courseJpaRepository, lectureJpaRepository, studentJpaRepository);
    }

    @Bean
    @Qualifier("courseRegistPubSubLockService")
    public CourseRegistService pubSubCourseRegistService(
        CourseCreationService courseCreationService,
        LockManager lockManager,
        @Qualifier("pubSubLock") DistributedLock pubSubLock,
        CourseJpaRepository courseJpaRepository,
        LectureJpaRepository lectureJpaRepository,
        StudentJpaRepository studentJpaRepository) {
        return new CourseRegistService(courseCreationService, lockManager, pubSubLock,
            courseJpaRepository, lectureJpaRepository, studentJpaRepository);
    }

    @Bean
    @Qualifier("courseRegistSpinLockService")
    public CourseRegistService spinCourseRegistService(
        CourseCreationService courseCreationService,
        LockManager lockManager,
        @Qualifier("spinLock") DistributedLock spinLock,
        CourseJpaRepository courseJpaRepository,
        LectureJpaRepository lectureJpaRepository,
        StudentJpaRepository studentJpaRepository) {
        return new CourseRegistService(courseCreationService, lockManager, spinLock,
            courseJpaRepository, lectureJpaRepository, studentJpaRepository);
    }

    @Bean
    @Qualifier("courseRegistNoLockService")
    public CourseRegistService noLockCourseRegistService(
        CourseCreationService courseCreationService,
        LockManager lockManager,
        @Qualifier("noLock") DistributedLock noLock,
        CourseJpaRepository courseJpaRepository,
        LectureJpaRepository lectureJpaRepository,
        StudentJpaRepository studentJpaRepository) {
        return new CourseRegistService(courseCreationService, lockManager, noLock,
            courseJpaRepository, lectureJpaRepository, studentJpaRepository);
    }

    @Bean
    @Qualifier("courseRegistServiceWithSemaphore")
    public CourseRegistServiceWithSemaphore redissonCourseRegistServiceWithSemaphore(
        SeatGate seatGate,
        CourseCreationService courseCreationService,
        LockManager lockManager,
        @Qualifier("redissonLock") DistributedLock redissonLock,
        CourseJpaRepository courseJpaRepository,
        LectureJpaRepository lectureJpaRepository,
        StudentJpaRepository studentJpaRepository) {
        return new CourseRegistServiceWithSemaphore(seatGate, courseCreationService,
            lockManager, redissonLock, courseJpaRepository, lectureJpaRepository,
            studentJpaRepository);
    }

}
