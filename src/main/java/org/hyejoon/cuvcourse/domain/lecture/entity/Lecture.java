package org.hyejoon.cuvcourse.domain.lecture.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hyejoon.cuvcourse.global.entity.BaseTimeEntity;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "lectures")
public class Lecture extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String lectureTitle;

    @Column(nullable = false)
    private String professorName;

    @Column(nullable = false)
    private int credits;

    @Column(nullable = false)
    private int capacity;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    @Column(nullable = false)
    private int total;

    public Lecture(String lectureTitle, String professorName, int credits, int capacity) {
        this.lectureTitle = lectureTitle;
        this.professorName = professorName;
        this.credits = credits;
        this.capacity = capacity;
    }
}
