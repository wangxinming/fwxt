package com.wxm.interceptor;

import com.wxm.controller.AuditController;
import com.wxm.model.OAUser;
import com.wxm.service.UserService;
import com.wxm.util.exception.OAException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
@Component
public class SessionInterceptor implements HandlerInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditController.class);
    @Autowired
    private UserService userService;
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        // 登录不做拦截
        if (httpServletRequest.getRequestURI().contains("/auth/login")
                || httpServletRequest.getRequestURI().contains("/auth/logout")) {
            return false;
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        com.wxm.entity.User loginUser=(com.wxm.entity.User)httpServletRequest.getSession().getAttribute("loginUser");
        if(null == loginUser) {
            LOGGER.error("用户未登录");
            throw new OAException(1101,"用户未登录");
        }else{
            if (userService == null) {//解决service为null无法注入问题
                BeanFactory factory = WebApplicationContextUtils.getRequiredWebApplicationContext(httpServletRequest.getServletContext());
                userService = (UserService) factory.getBean("userServiceImpl");
            }
            OAUser oaUser = userService.getUserById(loginUser.getId());
            if(oaUser != null && oaUser.getParentId() != loginUser.getParentId()){
                LOGGER.error("用户在其他地址登录");
                throw new OAException(1101,"用户在其他地址登录");
            }
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}
