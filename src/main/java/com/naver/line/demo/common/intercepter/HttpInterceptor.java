package com.naver.line.demo.common.intercepter;

import com.naver.line.demo.common.exceptions.UnauthorizedException;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HttpInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String X_USER_ID = request.getHeader("X-USER-ID");
        if (X_USER_ID == null) {
            throw new UnauthorizedException("해당 작업을 수행할 권한이 없습니다.");
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        return;
    }
}
