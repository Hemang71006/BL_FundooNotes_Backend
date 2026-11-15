
package com.fundoonotes.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Logging and Debugging - Request Interceptor
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Logs incoming HTTP requests and completed responses.
 * Records request method, URI, response status, and execution time.
 * Does not log request bodies, query parameters, or sensitive credentials.
 */

@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger =
            LoggerFactory.getLogger(RequestLoggingInterceptor.class);

    private static final String START_TIME_ATTRIBUTE =
            "requestStartTime";

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) {

        request.setAttribute(
                START_TIME_ATTRIBUTE,
                System.currentTimeMillis()
        );

        logger.info(
                "Incoming request: method={}, path={}",
                request.getMethod(),
                request.getRequestURI()
        );

        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception exception) {

        Object startTimeAttribute =
                request.getAttribute(START_TIME_ATTRIBUTE);

        long executionTime = 0;

        if (startTimeAttribute instanceof Long startTime) {
            executionTime = System.currentTimeMillis() - startTime;
        }

        logger.info(
                "Completed request: method={}, path={}, status={}, duration={}ms",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                executionTime
        );

        if (exception != null) {
            logger.error(
                    "Request processing failed: method={}, path={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    exception
            );
        }
    }
}