package com.cn.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;

public class Json {
    public static String toJson(Object object) {
        String result = "";
        if (null == object) {
            return result;
        } else {
            ObjectMapper objectMapper = new ObjectMapper();

            try {
                objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                result = objectMapper.writeValueAsString(object);
            } catch (Exception var4) {
                var4.printStackTrace();
            }

            return result;
        }
    }

    public static Object fromJson(String requestStr, Class clazz) {
        if (StringUtils.isEmpty(requestStr)) {
            return null;
        } else {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            Object object = null;

            try {
                object = objectMapper.readValue(requestStr, clazz);
            } catch (Exception var5) {
                var5.printStackTrace();
            }

            return object;
        }
    }

    public static Object fromJson(String requestStr, Class collectionClazz, Class<?>... elementClazzes) {
        if (StringUtils.isEmpty(requestStr)) {
            return null;
        } else {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            Object object = null;

            try {
                JavaType javaType = objectMapper.getTypeFactory().constructParametricType(collectionClazz, elementClazzes);
                object = objectMapper.readValue(requestStr, javaType);
            } catch (Exception var6) {
                var6.printStackTrace();
            }

            return object;
        }
    }
}
