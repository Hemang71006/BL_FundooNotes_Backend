package com.fundoonotes.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*
 * Project Name : Fundoo Notes Backend
 * Description  : Verifies logging and timing advice preserve method behavior.
 * Author       : Hemang
 * Date         : 27-11-2026
 */
class AspectTest {

    @Test
    void loggingAdviceReadsMethodMetadataWithoutChangingExecution() {

        JoinPoint joinPoint = mock(JoinPoint.class);
        Signature signature = mock(Signature.class);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringTypeName())
                .thenReturn("com.fundoonotes.service.TestService");
        when(signature.getName()).thenReturn("testMethod");

        LoggingAspect aspect = new LoggingAspect();

        aspect.logBeforeMethod(joinPoint);
        aspect.logAfterReturning(joinPoint);
        aspect.logAfterThrowing(joinPoint, new IllegalArgumentException());

        verify(joinPoint, org.mockito.Mockito.atLeastOnce())
                .getSignature();
    }

    @Test
    void performanceAdviceReturnsOriginalMethodResult() throws Throwable {

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringTypeName())
                .thenReturn("com.fundoonotes.controller.TestController");
        when(signature.getName()).thenReturn("testEndpoint");
        when(joinPoint.proceed()).thenReturn("result");

        Object result = new PerformanceAspect()
                .measureExecutionTime(joinPoint);

        assertEquals("result", result);
        verify(joinPoint).proceed();
    }

    @Test
    void performanceAdvicePropagatesOriginalException() throws Throwable {

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);
        IllegalStateException failure =
                new IllegalStateException("original failure");

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringTypeName())
                .thenReturn("com.fundoonotes.service.TestService");
        when(signature.getName()).thenReturn("failingMethod");
        when(joinPoint.proceed()).thenThrow(failure);

        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> new PerformanceAspect()
                        .measureExecutionTime(joinPoint)
        );

        assertEquals(failure, thrown);
        verify(joinPoint).proceed();
    }
}
