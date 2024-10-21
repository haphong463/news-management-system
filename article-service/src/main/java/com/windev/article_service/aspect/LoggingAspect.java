package com.windev.article_service.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Before("execution(* com.windev.article_service.service.impl.*.*(..))")
    public void logBeforeMethod(JoinPoint joinPoint) {
        log.info("Starting method --> {}", joinPoint.getSignature().getName());
    }

    @AfterReturning(pointcut = "execution(* com.windev.article_service.service.impl.*.*(..))", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        log.info("Completed method --> {}", joinPoint.getSignature().getName() + " with result: " + result);
    }

    @AfterThrowing
    public void logAfterThrowing(JoinPoint joinPoint, Throwable error){
        log.info("Exception in method --> {}", joinPoint.getSignature().getName() + " with cause: " + error.getMessage());
    }
}
