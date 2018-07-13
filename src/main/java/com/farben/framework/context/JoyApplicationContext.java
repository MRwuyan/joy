package com.farben.framework.context;

import com.farben.framework.annotation.JoyAutowired;
import com.farben.framework.annotation.JoyController;
import com.farben.framework.annotation.JoyService;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
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
            populate();
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

    /**
     * 依赖注入
     */
    private void populate() {
        //首先判断ioc容器中是否有类
        if (instanceMapping.isEmpty()) {
            return;
        }

        for (Map.Entry<String,Object> entry:instanceMapping.entrySet()) {
            //把所有的属性全都取出来,包括私有属性
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field filed:fields) {
                if (filed.isAnnotationPresent(JoyAutowired.class)) {
                    JoyAutowired autowired = filed.getAnnotation(JoyAutowired.class);
                    String id = autowired.value().trim();
                    //如果id为空,也就是说,自己没有设置
                    if ("".equals(id)) {
                        id = filed.getType().getName();
                    }
                    filed.setAccessible(true);//把私有变量开放访问权限
                    try {
                        filed.set(entry.getValue(), instanceMapping.get(id));

                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        continue;
                    }
                } else {
                    continue;
                }
            }
        }

    }

    private void doCreateBean() {
        //检查注册信息
        if (classCache.size() == 0) {
            return ;
        }
        try {
            for (String className:classCache ) {
                Class<?> clazz = Class.forName(className);
                //加了注解的才初始化
                if (clazz.isAnnotationPresent(JoyController.class)) {
                    //默认首字母类名小写
                    String id = lowerFirstChar(clazz.getSimpleName());
                    instanceMapping.put(id, clazz.getInterfaces());
                } else if (clazz.isAnnotationPresent(JoyService.class)) {
                    JoyService service = clazz.getAnnotation(JoyService.class);
                    //如果设置了自定义名字，就优先用它自己定义的名字
                    String id = service.value();
                    if (!"".equals(id)) {
                        instanceMapping.put(id, clazz.getInterfaces());
                        continue;
                    }
                    //如果是空的,就用默认规则
                    //1.类名首字母小写
                    //2.如果类是接口,可以根据类型匹配
                    Class<?>[] interfaces = clazz.getInterfaces();
                    //如果这个类实现了接口,就用接口的类型作为id
                    for (Class<?> i : interfaces) {
                        instanceMapping.put(i.getName(), clazz.getInterfaces());
                    }
                } else {
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 首字母小写
     * @param str
     * @return
     */
    private String lowerFirstChar(String str){
        char[] chars = str.toCharArray();
        chars[0] +=32;
        return String.valueOf(chars);
    }
    public Object getBean(String name) {
        return null;
    }

    public Map<String, Object> getAll() {
        return instanceMapping;
    }

}
