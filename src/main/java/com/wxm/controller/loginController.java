package com.wxm.controller;

import com.wxm.entity.User;
import com.wxm.model.OAUser;
import com.wxm.service.UserService;
import com.wxm.util.Md5Utils;
import org.activiti.engine.IdentityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/auth/")
public class loginController {

    @Autowired
    private UserService userService;
    @Autowired
    private IdentityService identityService;
    //登陆
    @RequestMapping(value = "login", method = RequestMethod.POST)
    @ResponseBody
    public Object login(@RequestParam(value="userName",required = true) String userName,
                        @RequestParam(value="password",required = true) String password,
                        HttpServletRequest request, HttpServletResponse response) throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) throw new Exception("用户未登录");
        Map<String, Object> result = new HashMap<String, Object>();
        OAUser oaUser = userService.selectByName(userName);
//        org.activiti.engine.identity.User user = identityService.createUserQuery().userId(userName).singleResult();
        User user= new User();
//                loginService.loginUser(userName);
        String psw = Md5Utils.getMd5(password);
        if(psw.equals(oaUser.getUserPwd())){
//            if(!user.getName().equals(userName)){
//                result.put("result","fail");
//                result.put("msg","用户名错误");
//                return result;
//            }
//            LoginUser loginUser=new LoginUser();
//            loginUser.setRoomPass(Convert.getFromBASE64(user.getPassword()));
            user.setId(oaUser.getUserId());
            user.setName(userName);
            user.setRealName(userName);
//            user.setMobile(user.getMobile());
//            user.setEmail(user.getEmail());
            request.getSession().setAttribute("loginUser", user);
            request.getSession().setAttribute("isLogin",true);
            result.put("userName",userName);
            result.put("result","success");
        }else if(user==null){
            result.put("result","fail");
            result.put("msg","用户不存在");
        }else{
            result.put("result","fail");
            result.put("msg","密码错误");
        }
        return result;
    }

    //登出
    @RequestMapping(value = "logout", method = RequestMethod.POST,produces = "application/json")
    @ResponseBody
    public void logOut(HttpServletRequest request, HttpServletResponse response) throws Exception{
        com.wxm.entity.User loginUser=(com.wxm.entity.User)request.getSession().getAttribute("loginUser");
        if(null == loginUser) throw new Exception("用户未登录");
        request.getSession().removeAttribute("isLogin");
        request.getSession().removeAttribute("loginUser");
    }
}
