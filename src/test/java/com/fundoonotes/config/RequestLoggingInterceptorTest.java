
package com.fundoonotes.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Logging and Debugging - Interceptor Test
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Tests the request logging interceptor.
 * Verifies request timing and completion handling.
 */

class RequestLoggingInterceptorTest {

    private RequestLoggingInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new RequestLoggingInterceptor();
    }

    @Test
    void shouldRecordRequestStartTime() {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        boolean result = interceptor.preHandle(
                request,
                response,
                new Object()
        );

        assertTrue(result);

        assertNotNull(
                request.getAttribute("requestStartTime")
        );
    }

    @Test
    void shouldCompleteRequestSuccessfully() throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        interceptor.preHandle(
                request,
                response,
                new Object()
        );

        response.setStatus(HttpServletResponse.SC_OK);

        interceptor.afterCompletion(
                request,
                response,
                new Object(),
                null
        );

        assertTrue(
                response.getStatus() == HttpServletResponse.SC_OK
        );
    }

    @Test
    void shouldHandleRequestWithException() throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        interceptor.preHandle(
                request,
                response,
                new Object()
        );

        Exception exception =
                new RuntimeException("Test exception");

        interceptor.afterCompletion(
                request,
                response,
                new Object(),
                exception
        );

        assertNotNull(exception);
    }
}