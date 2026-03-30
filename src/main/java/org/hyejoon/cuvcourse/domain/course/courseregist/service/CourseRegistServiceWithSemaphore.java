package org.hyejoon.cuvcourse.domain.course.courseregist.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hyejoon.cuvcourse.domain.course.courseregist.dto.CourseResponse;
import org.hyejoon.cuvcourse.domain.course.courseregist.exception.CourseRegistExceptionEnum;
import org.hyejoon.cuvcourse.domain.course.courseregist.seat.SeatGate;
import org.hyejoon.cuvcourse.domain.course.entity.Course;
import org.hyejoon.cuvcourse.domain.course.entity.CourseId;
import org.hyejoon.cuvcourse.domain.course.repository.CourseJpaRepository;
import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.hyejoon.cuvcourse.domain.lecture.repository.LectureJpaRepository;
import org.hyejoon.cuvcourse.domain.student.entity.Student;
import org.hyejoon.cuvcourse.domain.student.repository.StudentJpaRepository;
import org.hyejoon.cuvcourse.global.exception.BusinessException;
import org.hyejoon.cuvcourse.global.lock.DistributedLock;
import org.hyejoon.cuvcourse.global.lock.LockManager;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseRegistServiceWithSemaphore {

    private static final String COURSE_REGIST_LOCK_KEY = "course-service:regist-lock:";

    private final SeatGate seatGate;
    private final CourseCreationService courseCreationService;
    private final LockManager lockManager;
    private final DistributedLock distributedLock;
    private final CourseJpaRepository courseJpaRepository;
    private final LectureJpaRepository lectureJpaRepository;
    private final StudentJpaRepository studentJpaRepository;

    public CourseResponse registerCourse(long studentId, long lectureId) {
        log.debug("Lock type: {}", distributedLock.getType());
        log.debug("Use Redis Cache: {}", true);

        Lecture lecture = lectureJpaRepository.findById(lectureId)
            .orElseThrow(() -> new BusinessException(CourseRegistExceptionEnum.LECTURE_NOT_FOUND));

        // 0) redis에 키 없을 때만 정원 - 총원 수 저장
        seatGate.ensureInitialized(lectureId, () ->
            Math.max(0, lecture.getCapacity() - lecture.getTotal())
        );

        // 1) Fail-fast (좌석 획득 실패)
        if (!seatGate.tryAcquire(lectureId)) {
            throw new BusinessException(CourseRegistExceptionEnum.CAPACITY_FULL);
        }

        try {
            Student student = studentJpaRepository.findById(studentId)
                .orElseThrow(
                    () -> new BusinessException(CourseRegistExceptionEnum.STUDENT_NOT_FOUND));

            CourseId courseId = CourseId.of(lecture, student);

            String lockKey = COURSE_REGIST_LOCK_KEY + lectureId;

            Course course = lockManager.executeWithLock(distributedLock, lockKey, () -> {
                if (courseJpaRepository.existsById(courseId)) {
                    throw new BusinessException(CourseRegistExceptionEnum.ALREADY_REGISTERED);
                }
                return courseCreationService.createCourseIfAvailable(lecture, courseId);
            });

            return CourseResponse.from(course);
        } catch (RuntimeException e) {
            // 3) DB 등록 실패 시 좌석 보상
            seatGate.compensate(lectureId, lecture.getCapacity());
            throw e;
        }
    }
}
