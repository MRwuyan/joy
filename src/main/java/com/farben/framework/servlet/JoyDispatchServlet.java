package com.farben.framework.servlet;

import com.farben.framework.context.JoyApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class JoyDispatchServlet extends HttpServlet {
    private static final String LOCATION = "contextConfigLocation";
    private Map<String, Handler> handlerMapping = new HashMap<String, Handler>();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("do post");
        try {
            doDispatch(req,resp);
        } catch (Exception e) {
            resp.getWriter().write("500 Exception,MSG:"+ Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
        }
    }
    private void doDispatch(HttpServletRequest req, HttpServletResponse res) throws Exception{
        //从handlermappring中取
        Handler handler = getHandler(req);
        if (handler == null) {
            res.getWriter().write("404 Not found");
            return ;
        }
        //在取出来一个适配器
        HandlerAdapter handlerAdapter = getHandlerAdapter(handler);
        //再由适配器去调用具体方法
        handlerAdapter.handler(req,res,handler);
    }
    private HandlerAdapter getHandlerAdapter(Handler adapter){

        return null;
    }
    private Handler getHandler(HttpServletRequest request) {
        return null;
    }
    /**
     * 初始化我们的IOC容器
     * @param config
     * @throws ServletException
     */
    @Override
    public void init(ServletConfig config) throws ServletException {

        //ioc容器必先初始化
        //假装容器已启动
        JoyApplicationContext context = new JoyApplicationContext(config.getInitParameter(LOCATION));
    }

    /**
     * 请求解析
     * @param context
     */
    private void initMultipartResolver(JoyApplicationContext context){

    }

    /**
     * 多语言，国际化
     * @param context
     */
    private void initLocationResolver(JoyApplicationContext context) {

    }

    /**
     * 主题view
     * @param context
     */
    private void initThemeResolver(JoyApplicationContext context){

    }

    /**
     * 解析url和method关联
     * @param context
     */
    private void initHandlerMappings(JoyApplicationContext context){

    }

    /**
     * 适配器
     * @param context
     */
    private void initHandlerAdapters(JoyApplicationContext context) {

    }

    /**
     * 异常解析
     * @param context
     */
    private void initHandlerExceptionResolvers(JoyApplicationContext context){

    }

    /**
     * 视图转发
     * @param context
     */
    private void initViewNameTranslator(JoyApplicationContext context) {

    }

    /**
     * 解析模板内容（拿到服务器传输内容，生成HTML代码）
     * @param context
     */
    private void initViewResolvers(JoyApplicationContext context) {

    }
    private void initFlashMapManager(JoyApplicationContext context ){

    }

    private class Handler {

    }

    /**
     * 方法适配器
     */
    private class HandlerAdapter{
        public void handler(HttpServletRequest req,HttpServletResponse res,Handler handler){

        }
    }
}
