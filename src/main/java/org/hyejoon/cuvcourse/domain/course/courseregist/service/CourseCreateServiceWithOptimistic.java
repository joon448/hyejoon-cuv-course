package org.hyejoon.cuvcourse.domain.course.create.service;

import static org.hyejoon.cuvcourse.domain.course.exception.CourseExceptionEnum.REQUEST_FAILED;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.hyejoon.cuvcourse.domain.course.create.dto.CourseResponse;
import org.hyejoon.cuvcourse.global.exception.BusinessException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourseCreateServiceWithOptimistic {

    private final CourseCreateServiceWithOptimisticTx tx;

    public CourseResponse createCourseWithLock(Long studentId, Long lectureId) {
        int attempts = 0;
        int maxAttempts = 30;
        long backoff = 10; // ms
        while (true) {
            try {
                return tx.createTx(studentId, lectureId);
            } catch (ObjectOptimisticLockingFailureException | OptimisticLockException |
                     CannotAcquireLockException e) {
                if (++attempts > maxAttempts) {
                    throw new BusinessException(REQUEST_FAILED);
                }
                try {
                    Thread.sleep(Math.min(backoff, 100));
                } catch (InterruptedException ignored) {
                }
                backoff = Math.min(backoff * 2, 100);
            }
        }
    }
}
