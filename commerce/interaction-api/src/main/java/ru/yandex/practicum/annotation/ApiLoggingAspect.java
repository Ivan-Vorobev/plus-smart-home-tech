package ru.yandex.practicum.annotation;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class ApiLoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(ApiLoggingAspect.class);

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *) && execution(public * *(..))")
    public void restControllerMethods() {
    }

    @Around("restControllerMethods()")
    public Object logApiMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.toShortString();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        long startTime = System.currentTimeMillis();

        // Build parameter map (name: value)
        Map<String, Object> paramMap = new HashMap<>();
        for (int i = 0; i < parameterNames.length; i++) {
            paramMap.put(parameterNames[i], args[i]);
        }

        // Get HTTP request details
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String endpoint = attributes != null
                ? attributes.getRequest().getMethod() + " " + attributes.getRequest().getRequestURI()
                : "Unknown endpoint";

        logger.info("API method {} started for endpoint: {} with parameters: {}",
                methodName, endpoint, paramMap);

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            logger.info("API method {} completed for endpoint: {} in {} ms with result: {}",
                    methodName, endpoint, executionTime, result);

            return result;
        } catch (Throwable t) {
            logger.error("API method {} failed for endpoint: {} with exception: {}",
                    methodName, endpoint, t.getMessage(), t);
            throw t;
        }
    }
}