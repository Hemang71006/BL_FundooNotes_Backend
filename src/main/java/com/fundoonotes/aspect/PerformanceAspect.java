
package com.fundoonotes.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Aspect-Oriented Programming
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Measures the execution time of controller and service methods.
 * Uses around advice to execute the original method and calculate
 * the time taken by that method.
 */

@Aspect
@Component
@Order(1)
public class PerformanceAspect {

    private static final Logger logger =
            LoggerFactory.getLogger(PerformanceAspect.class);

    /**
     * Measures the execution time of the selected method.
     *
     * @param proceedingJoinPoint provides control over method execution
     * @return the original method's result
     * @throws Throwable if the original method throws an exception
     */
    @Around(
            "execution(* com.fundoonotes.controller..*(..)) || " +
                    "execution(* com.fundoonotes.service..*(..))"
    )
    public Object measureExecutionTime(
            ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        long startTime = System.nanoTime();

        try {

            // Execute the original method.
            return proceedingJoinPoint.proceed();

        } finally {

            // Calculate the execution time, even if the method fails.
            long endTime = System.nanoTime();

            long executionTime =
                    (endTime - startTime) / 1_000_000;

            String className =
                    proceedingJoinPoint.getSignature()
                            .getDeclaringTypeName();

            String methodName =
                    proceedingJoinPoint.getSignature().getName();

            logger.info(
                    "Execution time: {}.{} took {} ms",
                    className,
                    methodName,
                    executionTime
            );
        }
    }
}
