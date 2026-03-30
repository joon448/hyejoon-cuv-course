package org.hyejoon.cuvcourse.domain.course.cousecancel.service;

import lombok.RequiredArgsConstructor;
import org.hyejoon.cuvcourse.domain.course.courseregist.seat.SeatGate;
import org.hyejoon.cuvcourse.domain.course.cousecancel.exception.CourseCancelExceptionEnum;
import org.hyejoon.cuvcourse.domain.course.entity.Course;
import org.hyejoon.cuvcourse.domain.course.repository.CourseJpaRepository;
import org.hyejoon.cuvcourse.domain.lecture.repository.LectureJpaRepository;
import org.hyejoon.cuvcourse.global.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class CourseCancelService {

    private final CourseJpaRepository courseJpaRepository;
    private final LectureJpaRepository lectureJpaRepository;
    private final SeatGate seatGate;

    @Transactional
    public void courseCancel(Long lectureId, Long studentId) {
        Course course = courseJpaRepository.findByLectureAndStudent(lectureId, studentId)
            .orElseThrow(() -> new BusinessException(CourseCancelExceptionEnum.COURSE_NOT_FOUND));

        int capacity = course.getId().getLecture().getCapacity();

        courseJpaRepository.delete(course);
        lectureJpaRepository.decrementTotal(lectureId);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                seatGate.compensate(lectureId, capacity);
            }
        });
    }
}
