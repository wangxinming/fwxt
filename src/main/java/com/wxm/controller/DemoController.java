package com.wxm.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping(value = "/demo")
public class DemoController {
    @RequestMapping(value = "/list",method = {RequestMethod.POST},produces="application/json;charset=UTF-8")
    @ResponseBody
    public void list(HttpServletRequest request,HttpServletResponse response, Principal principal){
        try {
            response.sendRedirect("/demo/info");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/info",method = {RequestMethod.GET},produces="application/json;charset=UTF-8")
    @ResponseBody
    public Object info(HttpServletRequest request, HttpServletResponse response){
        return "hello world";
    }
}
