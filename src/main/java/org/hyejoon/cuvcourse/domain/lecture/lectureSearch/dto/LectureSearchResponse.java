package org.hyejoon.cuvcourse.domain.lecture.lectureSearch.dto;

public record LectureSearchResponse(Long id, String lectureTitle, String professorName, int credits,
                                    int totalStudents, int capacity) {

}
