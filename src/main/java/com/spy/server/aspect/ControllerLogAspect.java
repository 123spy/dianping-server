package com.spy.server.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ControllerLogAspect {

    @Around("execution(public * com.spy.server.controller..*(..))")
    public Object logControllerExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        log.info("控制器开始处理：类名={}，方法={}", className, methodName);
        try {
            Object result = joinPoint.proceed();
            long cost = System.currentTimeMillis() - startTime;
            log.info("控制器处理完成：类名={}，方法={}，耗时={}ms", className, methodName, cost);
            return result;
        } catch (Throwable throwable) {
            long cost = System.currentTimeMillis() - startTime;
            log.error("控制器处理异常：类名={}，方法={}，耗时={}ms，异常信息={}", className, methodName, cost, throwable.getMessage(), throwable);
            throw throwable;
        }
    }
}
