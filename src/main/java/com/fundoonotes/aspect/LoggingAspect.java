
package com.fundoonotes.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

/**
 * Project Name : Fundoo Notes Backend
 * Module       : Aspect-Oriented Programming
 * Author       : Hemang
 * Date         : 14-11-2026
 *
 * Description:
 * Provides centralized logging for controller and service methods.
 * Logs method execution, successful completion, and exceptions.
 *
 * This aspect keeps logging logic separate from business logic.
 */

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger =
            LoggerFactory.getLogger(LoggingAspect.class);

    /**
     * Defines the methods where logging should be applied.
     *
     * Matches methods inside the controller and service packages,
     * including their subpackages.
     */
    @Pointcut(
            "execution(* com.fundoonotes.controller..*(..)) || " +
                    "execution(* com.fundoonotes.service..*(..))"
    )
    public void applicationMethods() {
        // Pointcut method used by the logging advice.
    }

    /**
     * Executes before a matched method starts.
     *
     * @param joinPoint provides information about the executing method
     */
    @Before("applicationMethods()")
    public void logBeforeMethod(JoinPoint joinPoint) {

        String className =
                joinPoint.getSignature().getDeclaringTypeName();

        String methodName =
                joinPoint.getSignature().getName();

        logger.info("Method started: {}.{}",
                className, methodName);
    }

    /**
     * Executes after a matched method completes successfully.
     *
     * @param joinPoint provides information about the executing method
     */
    @AfterReturning("applicationMethods()")
    public void logAfterReturning(JoinPoint joinPoint) {

        String methodName =
                joinPoint.getSignature().getName();

        logger.info("Method completed successfully: {}",
                methodName);
    }

    /**
     * Executes when a matched method throws an exception.
     *
     * @param joinPoint provides information about the executing method
     * @param exception exception thrown by the method
     */
    @AfterThrowing(
            pointcut = "applicationMethods()",
            throwing = "exception"
    )
    public void logAfterThrowing(
            JoinPoint joinPoint,
            Throwable exception) {

        String className =
                joinPoint.getSignature().getDeclaringTypeName();

        String methodName =
                joinPoint.getSignature().getName();

        logger.error(
                "Exception in method: {}.{} - exception type: {}",
                className,
                methodName,
                exception.getClass().getSimpleName()
        );
    }
}
