package com.example.servicedispatcher;

import com.example.servicedispatcher.annotation.MethodName;
import com.example.servicedispatcher.annotation.ParamDto;
import com.example.servicedispatcher.annotation.ParamHashMap;
import com.example.servicedispatcher.dao.DAOFactory;
import com.example.servicedispatcher.result.Result;
import com.example.servicedispatcher.util.JavaBeanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 主程序调用外挂时，该工具类帮助创建服务对象、注入dao层、组织方法入参、调用指定的方法、返回结果、统一处理异常、记录方法运行时间
 */
public class ServiceDispatcher {

    /**
     * 缓存服务类对象
     * key是服务类的名称，包含路径名；value是服务类对象。
     */
    private static ConcurrentHashMap<String, Object> serviceObjectCache = new ConcurrentHashMap<String, Object>();
    /**
     * 缓存服务类的公有方法
     * key是服务类的名称，包含路径名；value是 HashMap，其中key是@HtMethodName中的方法名，value是对应的公有方法。
     */
    private static ConcurrentHashMap<String, HashMap<String, Method>> servicePublicMethodCache = new ConcurrentHashMap<String, HashMap<String, Method>>();
    /**
     * 日志
     */
    private static Logger logger = LoggerFactory.getLogger(ServiceDispatcher.class);

    /**
     * 主入口
     */
    public static String execute(Class<?> serviceClass, String methodName, HashMap<String, Object> paramMap) throws Exception {
        if (serviceClass == null) {
            throw new Exception("服务类型为空");
        }
        logger.info("开始方法 " + methodName);
        logger.info("接收到参数 paramMap: " + paramMap);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Result result;
        try {
            Object serviceObject = getServiceObject(serviceClass);
            Method method = getPublicMethodWithAnnotationHtMethodName(serviceClass, methodName);
            Object[] paramArray = getParamArray(method, paramMap);
            result = executeMethod(serviceObject, method, paramArray);
        } catch (Exception e) {
            logger.error("异常：{}", e.getMessage(), e);
            result = Result.fail(e.getMessage());
        } finally {
            stopWatch.stop();
            logger.info("结束调用方法 " + methodName + ", 耗时：{} ms", stopWatch.getLastTaskTimeMillis());
        }
        return result.getJsonString();
    }

    private static Object getServiceObject(Class<?> serviceClass) throws Exception {
        String serviceName = serviceClass.getName();
        if (!serviceObjectCache.containsKey(serviceName)) {
            Object serviceObject = serviceClass.newInstance();
            injectField(serviceClass, serviceObject);
            serviceObjectCache.putIfAbsent(serviceName, serviceObject);
        }
        return serviceObjectCache.get(serviceName);
    }

    /**
     * 注入 dao,model,logger
     */
    private static void injectField(Class<?> serviceClass, Object serviceObject) throws Exception {
        Field[] fields = serviceClass.getDeclaredFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            Object value = null;
            if (fieldName.contains("DAO")) {
                value = DAOFactory.getDao(fieldName);
            }
            if (value != null && field.getType().isAssignableFrom(value.getClass())) {
                field.setAccessible(true);
                field.set(serviceObject, value);
            }
        }
    }

    private static Method getPublicMethodWithAnnotationHtMethodName(Class<?> serviceClass, String methodName) throws Exception {
        String serviceName = serviceClass.getName();
        if (!servicePublicMethodCache.containsKey(serviceName)){
            Method[] methods = serviceClass.getDeclaredMethods();
            HashMap<String, Method> publicMethods = new HashMap<>();
            for (Method method : methods) {
                if (isPublicMethod(method) && method.isAnnotationPresent(MethodName.class)) {
                    MethodName annotation = method.getAnnotation(MethodName.class);
                    String name = annotation.value();
                    if (publicMethods.containsKey(name)) {
                        throw new Exception("类 " + serviceName + "中 @HtMethodName 标识的方法名 " + name + "重复");
                    }
                    publicMethods.put(name, method);
                }
            }
            servicePublicMethodCache.putIfAbsent(serviceName, publicMethods);
        }
        HashMap<String, Method> publicMethods = servicePublicMethodCache.get(serviceName);
        if (!publicMethods.containsKey(methodName)) {
            throw new Exception("类 " + serviceName + " 中方法  " + methodName + " 不存在，或者不是公有方法，或者没有 @HtMethodName 注解");
        }
        return publicMethods.get(methodName);
    }

    private static boolean isPublicMethod(Method m) {
        return Modifier.isPublic(m.getModifiers());
    }

    private static Object[] getParamArray(Method method, HashMap<String, Object> paramMap) throws Exception {
        Parameter[] parameters = method.getParameters();
        Object[] paramArray = new Object[parameters.length];
        if (parameters.length == 0 || paramMap == null || paramMap.isEmpty()) {
            return paramArray;
        }
        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            String paramName = param.getName();
            if ("arg0".equals(paramName)) {
                throw new Exception("反射获取到的方法 " + method.getName() + " 入参名称是 arg0");
            }
            Object paramObject = getParamFromMap(param, paramMap);
            paramArray[i] = paramObject;
        }
        return paramArray;
    }

    /**
     * 从主程序传来的 paramMap 中获取方法入参
     * paramMap 中包含 dbo
     */
    private static Object getParamFromMap(Parameter param, HashMap<String, Object> paramMap) throws Exception {
        Class<?> paramType = param.getType();
        String paramClassName = paramType.getName();
        String paramName = param.getName();

        if (param.isAnnotationPresent(ParamHashMap.class)) {
            if (!paramClassName.equals(HashMap.class.getName())) {
                throw new Exception("方法入参 " + paramName + " 的类型不是 HashMap");
            }
            return new HashMap<String, Object>(paramMap);
        } else if (param.isAnnotationPresent(ParamDto.class)) {
            return JavaBeanUtil.mapToBean(paramMap, paramType, true);
        }

        if (paramMap.containsKey(paramName)) {
            Object obj = paramMap.get(paramName);
            if (paramType.isAssignableFrom(obj.getClass())) {
                return obj;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static Result executeMethod(Object serviceObject, Method method, Object[] paramArray) throws Exception {
        try {
            return (Result) method.invoke(serviceObject, paramArray);
        } catch (InvocationTargetException ie) {
            throw (Exception) ie.getTargetException();
        }
    }

}
