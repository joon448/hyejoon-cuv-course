package org.hyejoon.cuvcourse.global.lock;

import java.time.Duration;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LettuceLockService {

    private final LockRedisRepository lockRedisRepository;

    @Value("${app.lock.ttl-millis:800}")
    private long ttlMillis;
    @Value("${app.lock.wait-millis:3000}")
    private long waitMillis;
    @Value("${app.lock.backoff-initial-ms:20}")
    private long backoffInitialMs;
    @Value("${app.lock.backoff-max-ms:100}")
    private long backoffMaxMs;

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }

    public void withLock(String key, Runnable body) {
        withLock(key, () -> {
            body.run();
            return null;
        });
    }

    public <T> T withLock(String key, Supplier<T> body) {
        String token = UUID.randomUUID().toString();
        long deadline = System.nanoTime() + Duration.ofMillis(waitMillis).toNanos();
        long backoff = backoffInitialMs;

        while (System.nanoTime() < deadline) {
            if (lockRedisRepository.tryLock(key, token, Duration.ofMillis(ttlMillis))) {
                try {
                    return body.get();
                } finally {
                    // 소유자 검증 후 안전 해제
                    lockRedisRepository.unlock(key, token);
                }
            }
            sleep(backoff);
            backoff = Math.min(backoff * 2, backoffMaxMs);
        }
        throw new LockNotAcquiredException("LOCK_NOT_ACQUIRED for key=" + key);
    }

    public static class LockNotAcquiredException extends RuntimeException {

        public LockNotAcquiredException(String m) {
            super(m);
        }
    }
}
