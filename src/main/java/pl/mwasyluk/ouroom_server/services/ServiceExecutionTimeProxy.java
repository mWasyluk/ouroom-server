package pl.mwasyluk.ouroom_server.services;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Slf4j
@Profile("dev")
@Aspect
@Component
public class ServiceExecutionTimeProxy {
    @Around("execution(* pl.mwasyluk.ouroom_server.services.*.*.*(..))")
    public Object logMethodUsage(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();
        long endTime = System.currentTimeMillis();

        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        log.debug("Service method {} executed in {}ms", className + "." + methodName, endTime - startTime);
        return proceed;
    }
}
