package com.cn.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class ServiceSorterHelper {
    public static final String SORT = "sort";
    public static final String DESC = "desc";

    public ServiceSorterHelper() {
    }

    public static Map<String, Object> build(String key, Object value) {
        Map<String, Object> _innerSortion = new LinkedHashMap();
        Integer direction = 1;
        if (value.equals("desc") || value.equals(-1)) {
            direction = -1;
        }

        _innerSortion.put(key, direction);
        return _innerSortion;
    }
}
