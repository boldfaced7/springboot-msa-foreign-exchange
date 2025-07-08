package com.boldfaced7.fxexchange.exchange.adapter.aop;

import com.boldfaced7.fxexchange.exchange.application.port.aop.DistributedLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DistributedLockAspect {

    private final RedissonClient redissonClient;

    @Around("@annotation(com.boldfaced7.fxexchange.exchange.application.port.aop.DistributedLock)")
    public Object lock(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        DistributedLock annotation = signature.getMethod().getAnnotation(DistributedLock.class);

        String lockKey = createLockKey(signature.getParameterNames(), joinPoint.getArgs(), annotation);
        RLock lock = redissonClient.getLock(lockKey);
        return tryLock(joinPoint, lock, annotation, lockKey);
    }

    private String createLockKey(
            String[] parameterNames,
            Object[] args,
            DistributedLock annotation
    ) {
        var parser  = new SpelExpressionParser();
        var context = new StandardEvaluationContext();

        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }
        var parsed = parser.parseExpression(annotation.key()).getValue(context, Object.class);

        return Optional.ofNullable(parsed)
                .map(i -> annotation.prefix() + i)
                .orElseThrow(() -> new IllegalArgumentException("키는 null이 될 수 없습니다."));
    }

    private static Object tryLock(
            ProceedingJoinPoint joinPoint,
            RLock lock,
            DistributedLock annotation,
            String lockKey
    ) throws Throwable {
        try {
            boolean isLocked = lock.tryLock(
                    annotation.waitTime(),
                    annotation.leaseTime(),
                    annotation.timeUnit()
            );
            if (!isLocked) {
                throw new IllegalStateException("락을 획득할 수 없습니다: " + lockKey);
            }
            log.info("분산 락 획득: {}", lockKey);
            return joinPoint.proceed();
        } catch (InterruptedException e) {
            throw new RuntimeException("락 획득 중 오류가 발생했습니다.", e);
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("분산 락 해제: {}", lockKey);
            }
        }
    }

}