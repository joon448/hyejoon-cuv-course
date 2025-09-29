package org.hyejoon.cuvcourse.domain.course.create.service;

import static org.hyejoon.cuvcourse.domain.course.exception.CourseExceptionEnum.ALREADY_REGISTERED;
import static org.hyejoon.cuvcourse.domain.course.exception.CourseExceptionEnum.LECTURE_NOT_FOUND;
import static org.hyejoon.cuvcourse.domain.course.exception.CourseExceptionEnum.STUDENT_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import org.hyejoon.cuvcourse.domain.course.create.dto.CourseResponse;
import org.hyejoon.cuvcourse.domain.course.entity.Course;
import org.hyejoon.cuvcourse.domain.course.entity.CourseId;
import org.hyejoon.cuvcourse.domain.course.exception.CourseExceptionEnum;
import org.hyejoon.cuvcourse.domain.course.repository.CourseJpaRepository;
import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.hyejoon.cuvcourse.domain.lecture.repository.LectureJpaRepository;
import org.hyejoon.cuvcourse.domain.student.entity.Student;
import org.hyejoon.cuvcourse.domain.student.repository.StudentJpaRepository;
import org.hyejoon.cuvcourse.global.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseCreateServiceWithOptimisticTx {

    private final CourseJpaRepository courseJpaRepository;
    private final LectureJpaRepository lectureJpaRepository;
    private final StudentJpaRepository studentJpaRepository;

    @Transactional
    public CourseResponse createTx(Long studentId, Long lectureId) {
        Lecture lecture = lectureJpaRepository.findByIdWithOptimisticLock(lectureId)
            .orElseThrow(() -> new BusinessException(LECTURE_NOT_FOUND));

        Student student = studentJpaRepository.findById(studentId)
            .orElseThrow(() -> new BusinessException(STUDENT_NOT_FOUND));

        CourseId courseId = CourseId.of(lecture, student);

        // 중복 신청 금지
        if (courseJpaRepository.existsById(courseId)) {
            throw new BusinessException(ALREADY_REGISTERED);
        }

        long currentHeadcount = courseJpaRepository.countByIdLecture(lecture);

        // 정원 초과 금지
        if (currentHeadcount >= lecture.getCapacity()) {
            throw new BusinessException(CourseExceptionEnum.CAPACITY_FULL);
        }

        Course course = Course.from(courseId);
        courseJpaRepository.save(course);

        return CourseResponse.from(course);
    }
}
