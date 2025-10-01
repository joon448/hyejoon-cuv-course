package org.hyejoon.cuvcourse.domain.lecture.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Lock;

public interface LectureJpaRepository extends JpaRepository<Lecture, Long> {

    @Modifying
    @Query(value = "UPDATE lectures SET total = total + 1 WHERE id = :id", nativeQuery = true)
    int incrementTotal(@Param("id") Long id);

    @Modifying
    @Query(value = "UPDATE lectures SET total = CASE WHEN total > 0 THEN total - 1 ELSE 0 END WHERE id = :id", nativeQuery = true)
    int decrementTotal(@Param("id") Long id);


    @Query(
        value = """
              SELECT * FROM lectures
              WHERE MATCH(lecture_title) AGAINST (:booleanQuery IN BOOLEAN MODE)
              ORDER BY id DESC
            """,
        countQuery = """
              SELECT COUNT(*) FROM lectures
              WHERE MATCH(lecture_title) AGAINST (:booleanQuery IN BOOLEAN MODE)
            """,
        nativeQuery = true
    )
    Page<Lecture> searchTitleBoolean(@Param("booleanQuery") String booleanQuery, Pageable pageable);

    @Query(
        value = """
              SELECT * FROM lectures
              WHERE MATCH(lecture_title) AGAINST (:keyword IN NATURAL LANGUAGE MODE)
              ORDER BY id DESC
            """,//MATCH(lecture_title) AGAINST (:keyword IN NATURAL LANGUAGE MODE) DESC,
        countQuery = """
              SELECT COUNT(*) FROM lectures
              WHERE MATCH(lecture_title) AGAINST (:keyword IN NATURAL LANGUAGE MODE)
            """,
        nativeQuery = true
    )
    Page<Lecture> searchTitleNatural(@Param("keyword") String keyword, Pageable pageable);

    @Query(
        value = """
              SELECT * FROM lectures
              WHERE lecture_title LIKE CONCAT('%', :keyword, '%')
              ORDER BY id DESC
            """,
        countQuery = """
              SELECT COUNT(*) FROM lectures
              WHERE lecture_title LIKE CONCAT('%', :keyword, '%')
            """,
        nativeQuery = true
    )
    Page<Lecture> searchTitleLike(@Param("keyword") String keyword, Pageable p);
    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("select l from Lecture l where l.id = :id")
    Optional<Lecture> findByIdWithOptimisticLock(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from Lecture l where l.id = :id")
    Optional<Lecture> findByIdWithPessimisticWrite(@Param("id") Long id);
}
