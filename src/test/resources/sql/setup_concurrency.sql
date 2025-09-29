-- 강의 1개
INSERT INTO lectures (id, lecture_title, professor_name, credits, capacity, created_at)
VALUES (1001, '동시성 제어 강의', 'professor', 3, 10, NOW());

-- 학생 30명
SET @n := 0;
INSERT INTO students (id, name, email, `password`, available_credits, created_at)
SELECT 2000 + (@n := @n + 1)                             AS id,
       CONCAT('학생', @n)                                AS name,
       CONCAT('s', @n, '@test.com')                      AS email,
       'pw'                                              AS `password`,
       18                                                AS available_credits,
       NOW()
FROM information_schema.columns
         LIMIT 30;