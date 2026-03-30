-- 제목 컬럼에 ngram 파서 기반 FULLTEXT 인덱스
CREATE FULLTEXT INDEX ft_lectures_title_ngram
ON lectures(lecture_title)
WITH PARSER ngram;