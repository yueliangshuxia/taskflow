package com.taskflow.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class LoggingInterceptor implements HandlerInterceptor {

    private final ThreadLocal<Long> startTime = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        startTime.set(System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        long duration = System.currentTimeMillis() - startTime.get();
        String username = request.getUserPrincipal() != null
                ? request.getUserPrincipal().getName() : "anonymous";
        log.info("{} {} {} {}ms [user={}]",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                duration,
                username);
        startTime.remove();
    }
}
