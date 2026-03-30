package org.hyejoon.cuvcourse.domain.course.courseregist.seat;


import java.util.List;
import java.util.function.LongSupplier;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SeatGate {

    private static final String SEATGATE_REDIS_KEY = "seats:course:";
    private final StringRedisTemplate redis;

    // 좌석 획득 (DECR)
    private final DefaultRedisScript<Long> acquireScript = new DefaultRedisScript<>(
        """
                local n = tonumber(redis.call('GET', KEYS[1]) or '-1')
                if n <= 0 then return 0 end
                redis.call('DECR', KEYS[1]); return 1
            """, Long.class);
    // 보상/취소 (INCR)
    private final DefaultRedisScript<Long> releaseWithCapacity = new DefaultRedisScript<>(
        """
                local cur = tonumber(redis.call('GET', KEYS[1]) or '0')
                local cap = tonumber(ARGV[1])
                if cur < cap then
                  return redis.call('INCR', KEYS[1])
                else
                  return cur
                end
            """, Long.class);

    private static String key(long lectureId) {
        return SEATGATE_REDIS_KEY + lectureId;
    }

    public boolean tryAcquire(long lectureId) {
        Long r = redis.execute(acquireScript, List.of(key(lectureId)));
        return Long.valueOf(1).equals(r);
    }

    public void compensate(long lectureId, int capacity) {
        redis.execute(releaseWithCapacity, List.of(key(lectureId)), String.valueOf(capacity));
    }

    // 키가 없을 때 초기 정원 수 설정
    public void ensureInitialized(long lectureId, LongSupplier initialSupplier) {
        String k = key(lectureId);
        String val = redis.opsForValue().get(k);
        if (val == null) {
            long init = Math.max(0, initialSupplier.getAsLong());
            redis.opsForValue().setIfAbsent(k, Long.toString(init));
        }
    }
}
