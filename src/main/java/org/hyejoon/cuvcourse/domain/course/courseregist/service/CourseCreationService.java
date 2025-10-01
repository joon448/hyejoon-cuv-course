package org.hyejoon.cuvcourse.domain.course.courseregist.service;

import lombok.RequiredArgsConstructor;
import org.hyejoon.cuvcourse.domain.course.courseregist.exception.CourseRegistExceptionEnum;
import org.hyejoon.cuvcourse.domain.course.entity.Course;
import org.hyejoon.cuvcourse.domain.course.entity.CourseId;
import org.hyejoon.cuvcourse.domain.course.repository.CourseJpaRepository;
import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.hyejoon.cuvcourse.domain.lecture.repository.LectureJpaRepository;
import org.hyejoon.cuvcourse.global.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseCreationService {

    private final CourseJpaRepository courseJpaRepository;
    private final LectureJpaRepository lectureJpaRepository;

    @Transactional
    public Course createCourseIfAvailable(Lecture lecture, CourseId courseId) {
        long currentHeadcount = courseJpaRepository.countByIdLecture(lecture);

        // 정원 초과 금지
        if (currentHeadcount >= lecture.getCapacity()) {
            throw new BusinessException(CourseRegistExceptionEnum.CAPACITY_FULL);
        }

        Course course = Course.from(courseId);
        Course saved = courseJpaRepository.save(course);

        // 총원 1 증가
        lectureJpaRepository.incrementTotal(lecture.getId());

        return saved;
    }
}
