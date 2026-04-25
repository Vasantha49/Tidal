package com.immomio.tidal.music.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Aspect for logging service method executions.
 * Provides cross-cutting concerns like entry logging, exit logging, exception handling, and execution time measurement.
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    /**
     * Pointcut for all methods in service classes.
     */
    @Pointcut("execution(* com.immomio.tidal.music.service.*.*(..))")
    public void serviceMethods() {}

    /**
     * Logs method entry before execution.
     */
    @Before("serviceMethods()")
    public void logMethodEntry() {
        log.info("Entering service method");
    }

    /**
     * Logs successful method exit after execution.
     */
    @AfterReturning("serviceMethods()")
    public void logMethodExit() {
        log.info("Exiting service method successfully");
    }

    /**
     * Logs exceptions thrown by service methods.
     *
     * @param ex the exception thrown
     */
    @AfterThrowing(pointcut = "serviceMethods()", throwing = "ex")
    public void logMethodException(Exception ex) {
        log.error("Exception in service method: {}", ex.getMessage(), ex);
    }

    /**
     * Measures and logs execution time of service methods.
     *
     * @param joinPoint the join point
     * @return the method result
     * @throws Throwable if the method throws an exception
     */
    @Around("serviceMethods()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long end = System.currentTimeMillis();
        log.info("Method {} executed in {} ms", joinPoint.getSignature().getName(), (end - start));
        return result;
    }
}
