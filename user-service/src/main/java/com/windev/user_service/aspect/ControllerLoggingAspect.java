package com.windev.user_service.aspect;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j
public class ControllerLoggingAspect {

    @Before("within(@org.springframework.stereotype.Controller *) || within(@org.springframework.web.bind.annotation.RestController *)")
    public void logBeforeController(JoinPoint joinPoint) {
        HttpServletRequest request = getHttpServletRequest();
        if (request == null) {
            log.warn("Không thể lấy HttpServletRequest");
            return;
        }

        log.info("Entering controller: {}.{}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName()
        );
        logHeaders(request);

    }

    @AfterReturning(pointcut = "within(@org.springframework.stereotype.Controller *) || within(@org.springframework.web.bind.annotation.RestController *)", returning = "result")
    public void logAfterController(JoinPoint joinPoint, Object result) {
        log.info("Exiting Controller: {}.{} with result = {}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                result
        );
    }

    private HttpServletRequest getHttpServletRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) requestAttributes).getRequest();
        }
        return null;
    }

    /**
     * Ghi log tất cả các header của yêu cầu
     */
    private void logHeaders(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames == null) {
            log.info("Không có header nào trong yêu cầu.");
            return;
        }
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            Enumeration<String> headers = request.getHeaders(headerName);
            while (headers.hasMoreElements()) {
                String headerValue = headers.nextElement();
                log.info("Header: {} = {}", headerName, headerValue);
            }
        }
    }


}
