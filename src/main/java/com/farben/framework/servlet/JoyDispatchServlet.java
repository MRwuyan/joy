package com.farben.framework.servlet;

import com.farben.framework.annotation.JoyController;
import com.farben.framework.annotation.JoyRequestMapping;
import com.farben.framework.annotation.JoyRequestParam;
import com.farben.framework.context.JoyApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class JoyDispatchServlet extends HttpServlet {
    private static final String LOCATION = "contextConfigLocation";
    private Map<String, Handler> handlerMapping = new HashMap<String, Handler>();
    private Map<Handler, HandlerAdapter> adapterMapping = new HashMap<Handler, HandlerAdapter>();

    //    private List<Handler> handlers = new ArrayList<Handler>();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("do post");
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            resp.getWriter().write("500 Exception,MSG:" + Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse res) throws Exception {

        //从handlermappring中取
        Handler handler = getHandler(req);
        if (handler == null) {
            res.getWriter().write("404 Not found");
            return;
        }
        //在取出来一个适配器
        HandlerAdapter ha = getHandlerAdapter(handler);
        //再由适配器去调用具体方法
        ha.handler(req, res, handler);
    }

    private HandlerAdapter getHandlerAdapter(Handler handler) {
        if (adapterMapping.isEmpty()) {
            return null;
        }
        return adapterMapping.get(handler);
    }

    private Handler getHandler(HttpServletRequest request) {
        if (handlerMapping.isEmpty()) {
            return null;
        }
        //循环
        String url = request.getRequestURI();
        String contextPath = request.getContextPath();
        url.replace(contextPath, "").replaceAll("/+", "/");

        return handlerMapping.get(url);
    }

    /**
     * 初始化我们的IOC容器
     *
     * @param config
     * @throws ServletException
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        //ioc容器必先初始化
        //假装容器已启动
        String initParameter = config.getInitParameter(LOCATION);


        JoyApplicationContext context = new JoyApplicationContext(config.getInitParameter(LOCATION));
        Map<String, Object> ioc = context.getAll();

        System.out.println(ioc.size());
        System.out.println(ioc.get("firstAction"));
        initMultipartResolver(context);
        initLocationResolver(context);
        initThemeResolver(context);
        //========重要========
        initHandlerMappings(context);
        initHandlerAdapters(context);
        //========重要========

        initHandlerExceptionResolvers(context);
        initViewNameTranslator(context);
        initViewResolvers(context);
        initFlashMapManager(context);

    }

    /**
     * 请求解析
     *
     * @param context
     */
    private void initMultipartResolver(JoyApplicationContext context) {
    }

    /**
     * 多语言，国际化
     *
     * @param context
     */
    private void initLocationResolver(JoyApplicationContext context) {
    }

    /**
     * 主题view
     *
     * @param context
     */
    private void initThemeResolver(JoyApplicationContext context) {
    }

    /**
     * 解析url和method关联
     *
     * @param context
     */
    private void initHandlerMappings(JoyApplicationContext context) {
        //只要是由controller修饰的类,里面的方法全部获取
        //而且这个方法方法上应该加了requestmapping注解,如果没加上这个注解,这个方法不能被外界所访问
        Map<String, Object> ioc = context.getAll();
        if (ioc.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
            if (!clazz.isAnnotationPresent(JoyController.class)) {
                continue;
            }
            String url = "";
            if (clazz.isAnnotationPresent(JoyRequestMapping.class)) {
                JoyRequestMapping requestMapping = clazz.getAnnotation(JoyRequestMapping.class);
                url = requestMapping.value();
            }
            Method[] methods = clazz.getMethods();
            for (Method method :
                    methods) {
                if (method.isAnnotationPresent(JoyRequestMapping.class)) {
                    continue;
                }
                JoyRequestMapping requestMapping = method.getAnnotation(JoyRequestMapping.class);
                String mappingUrl = url + requestMapping.value();
                handlerMapping.put(mappingUrl, new Handler(entry.getValue(), method));
            }
        }
        //RequestMapping会配置一个url,那么一个url就对应一个方法并将这个关系保存到map中

    }

    /**
     * 适配器
     * 动态匹配参数
     * 动态赋值
     * @param context
     */
    private void initHandlerAdapters(JoyApplicationContext context) {
        if (handlerMapping.isEmpty()) {
            return;
        }
        //参数类型作为key,参数索引作为值
        Map<String, Integer> paramMapping = new HashMap<String, Integer>();
        //只要取出来具体的某个方法
        for (Map.Entry<String, Handler> entry :
                handlerMapping.entrySet()) {
            //获取这个方法上面的所有参数
            Class<?>[] parameterTypes = entry.getValue().method.getParameterTypes();
            //有顺序
            for (int i= 0;i<parameterTypes.length;i++) {
                Class<?> type = parameterTypes[i];
                if (type == HttpServletRequest.class||
                        type == HttpServletResponse.class) {
                    paramMapping.put(type.getName(), i);
                }
            }
            //这里是匹配request和response
            Annotation[][] pa = entry.getValue().method.getParameterAnnotations();
            for (int i = 0; i<pa.length  ;i++) {
                for (Annotation a :
                        pa[i]) {
                    if (a instanceof JoyRequestParam) {
                        String paramName = ((JoyRequestParam) a).value();
                        paramMapping.put(paramName, i);
                    }
                }

            }
            adapterMapping.put(entry.getValue(), new HandlerAdapter(paramMapping));
        }
    }

    /**
     * 异常解析
     *
     * @param context
     */
    private void initHandlerExceptionResolvers(JoyApplicationContext context) {

    }

    /**
     * 视图转发
     *
     * @param context
     */
    private void initViewNameTranslator(JoyApplicationContext context) {
    }

    /**
     * 解析模板内容（拿到服务器传输内容，生成HTML代码）
     *
     * @param context
     */
    private void initViewResolvers(JoyApplicationContext context) {
    }

    private void initFlashMapManager(JoyApplicationContext context) {
    }

    private class Handler {
        protected Object controller;
        protected Method method;

        public Handler(Object controller, Method method) {
            this.controller = controller;
            this.method = method;
        }
    }

    /**
     * 方法适配器
     */
    private class HandlerAdapter {
        private Map<String, Integer> paramMapping ;
        public HandlerAdapter(Map<String, Integer> paramMapping) {
            this.paramMapping = paramMapping;
        }

        /**
         * 主要目的用反射调用url对应的method
         * @param req
         * @param res
         * @param handler
         */
        public void handler(HttpServletRequest req, HttpServletResponse res, Handler handler) throws InvocationTargetException, IllegalAccessException {

            //为什么要传req,res,handler
            Class<?>[] paramTypes = handler.method.getParameterTypes();
            //要想给参数赋值,只能通过索引号来找到具体的参数
            Object[] paramValues = new Object[paramTypes.length];
            Map<String, String[]> params = req.getParameterMap();
            for (Map.Entry<String, String[]> param:params.entrySet()){
                String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]", "").replaceAll(",\\s", ",");
                if (!this.paramMapping.containsKey(param.getKey())) {
                    continue;
                }
                Integer index = this.paramMapping.get(param.getKey());
                castStringValue(value, paramTypes[index]);
            }
            //request和response要赋值
            String reqName = HttpServletRequest.class.getName();
            if (this.paramMapping.containsKey(reqName)) {
                Integer reqIndex = this.paramMapping.get(reqName);
                paramValues[reqIndex] = req;
            }
            String respName = HttpServletResponse.class.getName();
            if (this.paramMapping.containsKey(respName)) {
                Integer reqIndex = this.paramMapping.get(respName);
                paramValues[reqIndex] = req;
            }
            handler.method.invoke(handler.controller, paramValues);
        }
    }

    private Object castStringValue(String value, Class<?> clazz) {
        if (clazz == String.class) {
            return clazz;
        } else if (clazz == Integer.class) {
            return Integer.valueOf(value);
        } else if (clazz == int.class) {
            return Integer.valueOf(value).intValue();
        } else {
            return null;
        }
    }
}
