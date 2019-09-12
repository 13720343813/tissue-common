package com.cn.util;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class ServiceQueryHelper {

    public static Map<String, Object> and(Map<String, Object> query, Map<String, Object> andQuery) {
        Map<String, Object> newQuery = query;
        String key;
        for (Iterator var3 = andQuery.keySet().iterator(); var3.hasNext(); newQuery = and(newQuery, key, andQuery.get(key))) {
            key = (String) var3.next();
        }
        return newQuery;
    }

    public static Map<String, Object> and(Map<String, Object> query, String key, Object value) {
        return and(query, key, value, null);
    }

    public static Map<String, Object> and(Map<String, Object> query, String key, Object value, String compare) {
        if (null == query) {
            query = new LinkedHashMap<>();
        }
        if (null == compare) {
            query.put(key, value);
        } else {
            Map<String, Object> _innertQuery = new LinkedHashMap<>();
            if (query.containsKey(key)) {
                Object _iq = query.get(key);
                if (_iq instanceof Map) {
                    _innertQuery = (Map<String, Object>) _iq;
                } else {
                    _innertQuery = new LinkedHashMap<>();
                }
                _innertQuery.put(compare, value);
                query.put(key, _innertQuery);
            }
        }
        return query;
    }

    public static Map<String, Object> or(Map<String, Object> query, String key, Object value) {
        return or(query, key, value, (String) null);
    }

    public static Map<String, Object> or(Map<String, Object> query, String key, Object value, String compare) {
        if (null == query) {
            query = new LinkedHashMap();
        }

        Map<String, Object> orQuery = (Map) ((Map) query).get("$or");
        if (null == orQuery) {
            orQuery = new LinkedHashMap();
        }

        ((Map) query).put("$or", and((Map) orQuery, key, value, compare));
        return (Map) query;
    }
}
