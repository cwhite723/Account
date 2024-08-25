package com.hayan.Account.redis;

import com.hayan.Account.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.hayan.Account.exception.ErrorCode.LOCK_ACQUISITION_FAILED;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class DistributedLockAop {

    private final RedissonClient redissonClient;
    private final AopForTransaction aopForTransaction;

    @Around("@annotation(distributedLock)")
    public Object lock(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        String lockKey = distributedLock.key();
        RLock rLock = redissonClient.getLock(lockKey);

        try {
            boolean available = rLock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), TimeUnit.SECONDS);
            if (!available) {
                log.warn("락 획득에 실패했습니다. 키: {}", lockKey);
                throw new CustomException(LOCK_ACQUISITION_FAILED);
            }

            log.info("락을 획득했습니다. 키: {}", lockKey);

            return aopForTransaction.proceed(joinPoint);
        } finally {
            if (rLock != null && rLock.isHeldByCurrentThread()) {
                rLock.unlock();
                log.info("락을 해제했습니다. 키: {}", lockKey);
            }
        }
    }
}
