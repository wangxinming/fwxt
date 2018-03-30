//package com.wxm.activiti.controller;
//
//import org.activiti.engine.IdentityService;
//import org.activiti.engine.identity.Group;
//import org.activiti.engine.identity.User;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//public class GroupController {
//    @Autowired
//    IdentityService identityService;
//    public void createGroup(){
//        Group group = identityService.newGroup("新用户ID");
//        identityService.saveGroup(group);
//    }
//}
