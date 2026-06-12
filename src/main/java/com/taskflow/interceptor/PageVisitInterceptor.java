package com.taskflow.interceptor;

import com.taskflow.entity.VisitLog;
import com.taskflow.dao.VisitLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.CompletableFuture;

public class PageVisitInterceptor implements HandlerInterceptor {

    @Autowired
    private VisitLogRepository visitLogRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        VisitLog log = new VisitLog();
        log.setPageUrl(request.getRequestURI());
        log.setIpAddress(request.getRemoteAddr());
        if (request.getUserPrincipal() != null) {
            log.setUsername(request.getUserPrincipal().getName());
        }
        CompletableFuture.runAsync(() -> visitLogRepository.save(log));
        return true;
    }
}
