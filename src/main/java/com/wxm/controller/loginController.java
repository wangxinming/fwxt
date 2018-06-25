package com.wxm.controller;

import com.wxm.entity.User;
import com.wxm.model.OAAudit;
import com.wxm.model.OAUser;
import com.wxm.service.AuditService;
import com.wxm.service.UserService;
import com.wxm.util.Md5Utils;
import com.wxm.util.exception.OAException;
import org.activiti.engine.IdentityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@RestController
@RequestMapping(value = "/auth/")
public class loginController {
    private static final Logger LOGGER = LoggerFactory.getLogger(loginController.class);
    @Autowired
    private UserService userService;
    @Autowired
    private AuditService auditService;
    @Autowired
    private IdentityService identityService;
    //登陆
    @RequestMapping(value = "login", method = RequestMethod.POST)
    @ResponseBody
    public Object login(@RequestParam(value="userName",required = true) String userName,
                        @RequestParam(value="password",required = true) String password,
                        HttpServletRequest request, HttpServletResponse response) throws Exception{
        Map<String, Object> result = new HashMap<String, Object>();
        OAUser oaUser = userService.selectByName(userName);
        if(oaUser != null) {
            Integer random = new Random().nextInt();
            User user = new User();
            String psw = Md5Utils.getMd5(password);
            if (psw.equals(oaUser.getUserPwd())) {
                user.setId(oaUser.getUserId());
                user.setName(oaUser.getUserName());
                user.setRealName(oaUser.getUserName());
                user.setParentId(random);
                request.getSession().setAttribute("loginUser", user);
                request.getSession().setAttribute("isLogin", true);
                result.put("userName", oaUser.getUserName());
                result.put("result", "success");
                OAUser oaUserUpdate = new OAUser();
                oaUserUpdate.setUserName(oaUser.getUserName());
                oaUserUpdate.setUserId(oaUser.getUserId());
                oaUserUpdate.setParentId(random);
                userService.update(oaUserUpdate);
            } else if (user == null) {
                result.put("result", "fail");
                result.put("msg", "用户不存在");
            } else {
                result.put("result", "fail");
                result.put("msg", "密码错误");
            }
        }else{
            result.put("result", "fail");
            result.put("msg", "用户不存在");
        }
        auditService.audit(new OAAudit(userName,String.format("登录系统")));
        LOGGER.info("用户：{}，登录时间：{}",userName,new Date());
        return result;
    }

    //登出
    @RequestMapping(value = "logout", method = RequestMethod.POST,produces = "application/json")
    @ResponseBody
    public void logOut(HttpServletRequest request, HttpServletResponse response) throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) return;

        auditService.audit(new OAAudit(loginUser.getName(),String.format("登出系统")));
        OAUser oaUserUpdate = new OAUser();
        oaUserUpdate.setUserId(loginUser.getId());
        oaUserUpdate.setParentId(0);
        userService.updateStatus(oaUserUpdate);
//        if(null == loginUser) throw new OAException(1101,"用户未登录");
        request.getSession().removeAttribute("isLogin");
        request.getSession().removeAttribute("loginUser");
        LOGGER.info("用户：{}，登出时间：{}",loginUser.getName(),new Date());
    }



}
