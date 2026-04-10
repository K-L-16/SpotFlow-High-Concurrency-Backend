package com.kl.aspect;

import com.kl.annotation.LogOperation;
import com.kl.dto.UserDTO;
import com.kl.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LogOperationAspect {

    @Around("@annotation(logOperation)")
    public Object around(ProceedingJoinPoint joinPoint, LogOperation logOperation) throws Throwable {
        long startTime = System.currentTimeMillis();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringTypeName();
        String methodName = signature.getName();
        String operation = logOperation.value();

        Object[] args = joinPoint.getArgs();

        Long userId = null;
        UserDTO user = UserHolder.getUser();
        if (user != null) {
            userId = user.getId();
        }

        try {
            Object result = joinPoint.proceed();

            long cost = System.currentTimeMillis() - startTime;
            log.info(
                    "Operation Log | Action: {} | UserId: {} | Method: {}.{} | Args: {} | Duration: {} ms | Result: SUCCESS",
                    operation,
                    userId,
                    className,
                    methodName,
                    Arrays.toString(args),
                    cost
            );

            return result;
        } catch (Throwable e) {
            long cost = System.currentTimeMillis() - startTime;
            log.error(
                    "Operation Log | Action: {} | UserId: {} | Method: {}.{} | Args: {} | Duration: {} ms | Result: FAILURE",
                    operation,
                    userId,
                    className,
                    methodName,
                    Arrays.toString(args),
                    cost,
                    e
            );
            throw e;
        }
    }
}