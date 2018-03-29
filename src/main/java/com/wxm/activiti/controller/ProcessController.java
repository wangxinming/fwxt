package com.wxm.activiti.controller;

import org.activiti.engine.FormService;
import org.activiti.engine.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/backstage/workflow/process/")
public class ProcessController {
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    FormService formService;


}
