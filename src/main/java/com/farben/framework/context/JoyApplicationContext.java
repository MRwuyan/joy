package com.farben.framework.context;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class JoyApplicationContext {
    private Map<String, Object> instanceMapping = new ConcurrentHashMap<String, Object>();
    private List<String> classCache = new ArrayList<String>();
    public JoyApplicationContext(String location) {
        InputStream input = null;
        try {
            //定位
            input = this.getClass().getClassLoader().getResourceAsStream(location);
            //载入
            Properties config = new Properties();
            config.load(input);
            //注册（把所有class找出来）
            String packageName = config.getProperty("scanPackage");
            doRegister(packageName);

            // 初始化 只要循环class
            doCreateBean();
            //注入
            doAutowired();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //首先加载配置文件


        //定位，载入，注册、初始化、注入、
    }
    //把所有符合条件的class找出来，注册到缓存中
    private void doRegister(String packageName) {
        URL url = this.getClass().getClassLoader().getResource("/" + packageName.replaceAll("\\.", "/"));
        File dir = new File(url.getFile());
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                //如果是一个文件夹继续递归
                doRegister(packageName + "." + file.getName());
            } else {
                classCache.add(packageName + "." + file.getName().replace(".class", "").trim());
            }
        }

    }
    private void doAutowired() {
    }

    private void doCreateBean() {
        //检查注册信息
        if (classCache.size() == 0) {
            return ;
        }
        try {

        } catch (Exception e) {
        }
    }

    public Object getBean(String name) {
        return null;
    }

    public Map<String, Object> getAll(String name) {
        return instanceMapping;
    }

}
