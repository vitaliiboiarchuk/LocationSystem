package com.example.locationsystem.aspect;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.example.locationsystem.exception.ControllerExceptions.*;
import org.springframework.web.util.WebUtils;

import java.lang.reflect.Parameter;

@Aspect
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CookieAspect {

    HttpServletRequest request;

    @Around("@annotation(com.example.locationsystem.aspect.GetAndValidUserId)")
    public Object validateUserId(ProceedingJoinPoint joinPoint) throws Throwable {

        Cookie userCookie = WebUtils.getCookie(request, "user");

        if (userCookie == null) {
            throw new NotLoggedInException("Not logged in");
        }

        long userId = Long.parseLong(userCookie.getValue());

        Parameter[] parameters = ((MethodSignature) joinPoint.getSignature()).getMethod().getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (parameter.getType().equals(Long.class) && parameter.getName().equals("userId")) {
                args[i] = userId;
            }
        }
        return joinPoint.proceed(args);
    }
}

