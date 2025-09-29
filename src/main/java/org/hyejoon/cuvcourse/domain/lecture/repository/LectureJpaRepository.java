package org.hyejoon.cuvcourse.domain.lecture.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LectureJpaRepository extends JpaRepository<Lecture, Long> {

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("select l from Lecture l where l.id = :id")
    Optional<Lecture> findByIdWithOptimisticLock(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from Lecture l where l.id = :id")
    Optional<Lecture> findByIdWithPessimisticWrite(@Param("id") Long id);
}
