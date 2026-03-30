package org.hyejoon.cuvcourse.domain.lecture.lectureSearch.service;

import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.hyejoon.cuvcourse.domain.lecture.entity.Lecture;
import org.hyejoon.cuvcourse.domain.lecture.lectureSearch.dto.LectureSearchResponse;
import org.hyejoon.cuvcourse.domain.lecture.repository.LectureJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LectureSearchService {

    private final LectureJpaRepository lectureJpaRepository;

    public static String toBooleanQuery(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return "";
        }
        return Arrays.stream(keyword.split(" "))
            .map(tok -> "+" + tok)     // 접두어 매칭 비활성
            .collect(Collectors.joining(" "));
    }

    public Page<LectureSearchResponse> searchByKeyword(String keyword, Pageable pageable) {

        //String booleanQuery = toBooleanQuery(keyword);

        Page<Lecture> lectures = lectureJpaRepository.searchTitleNatural(keyword, pageable);

        return lectures.map(lecture -> new LectureSearchResponse(
            lecture.getId(),
            lecture.getLectureTitle(),
            lecture.getProfessorName(),
            lecture.getCredits(),
            lecture.getTotal(),
            lecture.getCapacity()
        ));
    }
}
