package com.brpc.client;

import com.mornsnow.common.util.BeanUtils;
import com.mornsnow.starter.log.LogUtil;
import com.mornsnow.starter.log.QcLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <strong>描述：</strong> <br>
 * <strong>功能：</strong><br>
 * <strong>使用场景：</strong><br>
 * <strong>注意事项：</strong>
 * <ul>
 * <li></li>
 * </ul>
 *
 * @author jianyang 2017/12/8
 */
@Api(tags = {"RpcTest"})
@RestController
@RequestMapping("/RpcTest")
public class RpcTest implements ApplicationContextAware {

    private static final QcLog LOG = LogUtil.getLogger(RpcTest.class);

    private ApplicationContext applicationContext;

    @Value("${spring.application.name}")
    private String appName;

    @ApiOperation(value = "test")
    @RequestMapping(value = "/test", method = RequestMethod.POST)
    public Object test(String password, String className, String methodName, RequestType requestType, @RequestBody Object request) throws Exception {

        String ckeckResult = authCheck(password);

        if (ckeckResult != null) {
            return ckeckResult;
        }
        if (requestType.equals(RequestType.GET_PARAM)) {
            return getParam(className, methodName);
        }

        Object bean = applicationContext.getBean(className);

        // 查找 methodName对应的method
        Method function = null;
        Method[] methods = bean.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                function = method;
                break;
            }
        }

        if (function.getParameterCount() == 0) {
            // 无参数
            Object result = function.invoke(bean);
            return result;
        } else if (function.getParameterCount() == 1) {
            Object result = function.invoke(bean, BeanUtils.copyBean(request, function.getParameterTypes()[0]));
            return result;
        }

        return null;
    }

    private String authCheck(String password) {

        if (StringUtils.isEmpty(password)) {
            return "你是坏人";
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        String check = appName.substring(0, 3) + format.format(new Date());
        if (!password.equals(check)) {
            return "你是坏人";
        }
        return null;
    }

    private Object getParam(String className, String methodName) throws Exception {

        Object bean = applicationContext.getBean(className);

        // 查找 methodName对应的method
        Method function = null;
        Method[] methods = bean.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                function = method;
                break;
            }
        }

        if (function.getParameterCount() == 0) {
            // 无参数
            return "无参数";
        } else if (function.getParameterCount() == 1) {
            Class clazz = function.getParameterTypes()[0];
            Object o = clazz.newInstance();
            buildJson(o, "");
            return o;
        }
        return "";
    }


    private void buildJson(Object object, String parent) {
        try {
            Class clazz = object.getClass();
            Field[] fields = object.getClass().getDeclaredFields();
            for (Field field : fields) {
                String fieldName = field.getName();

                if (fieldName.equals(parent)) {
                    continue;
                }

                String fieldType = getFieldType(field);

                if (fieldType == null) {
                    continue;
                }
                if (fieldType.equals(JAVA_TYPE_LIST)) {
                    List list = new ArrayList();
                    Object newInstance = Class.forName(getInnerClass(field.getGenericType().getTypeName())).newInstance();
                    list.add(newInstance);
                    Method method = clazz.getMethod("set" + change(fieldName), List.class);
                    method.invoke(object, list);
                    buildJson(newInstance, fieldName);
                } else if (fieldType.equals(JAVA_TYPE_BOOLEAN)) {
                    Method method = clazz.getMethod("set" + change(fieldName), Boolean.class);
                    method.invoke(object, false);
                } else if (fieldType.equals(JAVA_TYPE_STRING)) {
                    Method method = clazz.getMethod("set" + change(fieldName), String.class);
                    method.invoke(object, "");
                } else if (fieldType.equals(JAVA_TYPE_INTEGER)) {
                    Method method = clazz.getMethod("set" + change(fieldName), Integer.class);
                    method.invoke(object, 0);
                } else if (fieldType.equals(JAVA_TYPE_FLOAT)) {
                    Method method = clazz.getMethod("set" + change(fieldName), Float.class);
                    method.invoke(object, 0F);
                } else if (fieldType.equals(JAVA_TYPE_DOUBLE)) {
                    Method method = clazz.getMethod("set" + change(fieldName), Double.class);
                    method.invoke(object, 0D);
                } else if (fieldType.equals(JAVA_TYPE_LONG)) {
                    Method method = clazz.getMethod("set" + change(fieldName), Long.class);
                    method.invoke(object, 0L);
                } else if (fieldType.equals(JAVA_TYPE_OBJECT)) {
                    Class clz = Class.forName(field.getGenericType().getTypeName());
                    Method method = clazz.getMethod("set" + change(fieldName), clz);
                    Object obj = clz.newInstance();
                    method.invoke(object, obj);
                    buildJson(obj, fieldName);
                }
            }

        } catch (Exception e) {
            LOG.error("build param json faild", e);
        }
    }


    public static String change(String src) {
        if (src != null) {
            StringBuffer sb = new StringBuffer(src);
            sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
            return sb.toString();
        } else {
            return null;
        }
    }

    public static String getInnerClass(String className) {
        if (!className.contains("<") || !className.contains(">")) {
            return null;
        }
        return className.substring(className.indexOf("<") + 1, className.indexOf(">"));
    }

    private static final String JAVA_TYPE_OBJECT = "Object";
    private static final String JAVA_TYPE_LIST = "List";
    private static final String JAVA_TYPE_INTEGER = "Integer";
    private static final String JAVA_TYPE_DOUBLE = "Double";
    private static final String JAVA_TYPE_LONG = "Long";
    private static final String JAVA_TYPE_FLOAT = "Float";
    private static final String JAVA_TYPE_BOOLEAN = "Boolean";
    private static final String JAVA_TYPE_STRING = "String";


    private String getFieldType(Field field) {
        String typeName = field.getGenericType().getTypeName();
        if (typeName.startsWith("java.util.List")) {
            return JAVA_TYPE_LIST;
        } else if (typeName.equals("java.lang.String")) {
            return JAVA_TYPE_STRING;
        } else if (typeName.equals("java.lang.Integer")) {
            return JAVA_TYPE_INTEGER;
        } else if (typeName.equals("java.lang.Long")) {
            return JAVA_TYPE_LONG;
        } else if (typeName.equals("java.lang.Double")) {
            return JAVA_TYPE_DOUBLE;
        } else if (typeName.equals("java.lang.Float")) {
            return JAVA_TYPE_FLOAT;
        } else if (typeName.equals("java.lang.Boolean")) {
            return JAVA_TYPE_BOOLEAN;
        } else if (typeName.startsWith("com.mornsnow")) {
            return JAVA_TYPE_OBJECT;
        }
        return null;
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


}
