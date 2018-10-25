package com.wulee.administrator.zuji.utils;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wulee on 2017/2/28 16:12
 */

public class GsonUtil {

    //将Json数据解析成相应的映射对象
    public static <T> T parseJsonWithGson(String jsonData, Class<T> type) {
        Gson gson = new Gson();
        T result = gson.fromJson(jsonData, type);
        return result;
    }

    public static String createGsonString(Object object) {
        Gson gson = new Gson();
        String gsonString = gson.toJson(object);
        return gsonString;
    }

    public static <T> T changeGsonToBean(String gsonString, Class<T> cls) {
        Gson gson = new Gson();
        T t = gson.fromJson(gsonString, cls);
        return t;
    }

    public static <T> List<T> changeGsonToList(String gsonString, Class<T> cls) {
        Gson gson = new Gson();
        List<T> list = gson.fromJson(gsonString, new TypeToken<List<T>>() {
        }.getType());
        return list;
    }


    public static <T> List<Map<String, T>> changeGsonToListMaps(
            String gsonString) {
        List<Map<String, T>> list = null;
        Gson gson = new Gson();
        list = gson.fromJson(gsonString, new TypeToken<List<Map<String, T>>>() {
        }.getType());
        return list;
    }

    public static <T> Map<String, T> changeGsonToMaps(String gsonString) {
        Map<String, T> map = null;
        Gson gson = new Gson();
        map = gson.fromJson(gsonString, new TypeToken<Map<String, T>>() {
        }.getType());
        return map;
    }


    /**
     * 将javaBean转换成Map
     *
     * @param javaBean javaBean
     * @return Map对象
     */
    public static Map<String, String> BeantoMap(Object javaBean) {
        Map<String, String> result = new HashMap<>();
        Method[] methods = javaBean.getClass().getDeclaredMethods();

        for (Method method : methods) {
            try {
                if (method.getName().startsWith("get")) {
                    String field = method.getName();//getName  getPassword
                    field = field.substring(field.indexOf("get") + 3);//Name  Password
                    field = field.toLowerCase().charAt(0) + field.substring(1);//name password
                    Object value = method.invoke(javaBean, (Object[]) null);
                    result.put(field, null == value ? "" : value.toString());
                }
            } catch (Exception e) {
            }
        }

        return result;
    }

    /**
     * 将javaBean转换成JSONObject
     *
     * @param bean javaBean
     * @return json对象
     */
    public static JSONObject toJSON(Object bean) {
        return new JSONObject(BeantoMap(bean));
    }
}
