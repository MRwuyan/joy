package com.farben.demo.mvc.action;

import com.farben.demo.service.INamedService;
import com.farben.demo.service.IService;
import com.farben.framework.annotation.JoyAutowired;
import com.farben.framework.annotation.JoyController;
import com.farben.framework.annotation.JoyRequestMapping;
import com.farben.framework.annotation.JoyRequestParam;
import com.sun.deploy.net.HttpResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@JoyController
@JoyRequestMapping("/web")
public class FirstAction {
    @JoyAutowired
    private IService service;

    @JoyAutowired("myName")
    private INamedService namedService;
    @JoyRequestMapping("/query.json")
    public void query(HttpServletRequest request, HttpServletResponse response, @JoyRequestParam("name") String name){


    }
}
