package org.hyejoon.cuvcourse.domain.lecture.lectureSearch.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.hyejoon.cuvcourse.domain.lecture.lectureSearch.dto.LectureSearchResponse;
import org.hyejoon.cuvcourse.domain.lecture.lectureSearch.service.LectureSearchService;
import org.hyejoon.cuvcourse.global.dto.GlobalResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@RequestMapping("/api/lectures/search")
public class LectureSearchController {

    private final LectureSearchService lectureSearchService;

    @GetMapping
    public GlobalResponse<Page<LectureSearchResponse>> getAllLectures(
        @RequestParam String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));

        return GlobalResponse.ok("조회 성공", lectureSearchService.searchByKeyword(keyword, pageable));
    }
}
