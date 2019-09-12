package com.cn.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;

public class RestBasicServiceCache {
    public static Cache<String, Object> cache;
    static {
        cache = CacheBuilder.newBuilder().expireAfterWrite(30L, TimeUnit.MINUTES).build();
    }
}
