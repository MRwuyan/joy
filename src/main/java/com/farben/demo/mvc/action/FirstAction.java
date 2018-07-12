package com.farben.demo.mvc.action;

import com.farben.demo.service.INamedService;
import com.farben.demo.service.IService;
import com.farben.framework.annotation.JoyAutowired;
import com.farben.framework.annotation.JoyController;

@JoyController
public class FirstAction {
    @JoyAutowired
    private IService service;

    @JoyAutowired
    private INamedService namedService;
}
