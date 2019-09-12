package com.cn.service;

import com.cn.common.ServiceResponseCode;
import com.cn.util.Json;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.Callable;

public class RestBasicService {
    private String serviceRequest;
    private String serviceAddress;
    private String serviceEntry;
    private String serviceToken;
    private Map<String, Object> serviceResult = new HashMap<String, Object>();
    private static RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> getCollection(Map<String, Object> query) {
        this.setServiceRequestQuery(query,null,null);
        return this.requestCollectionList();
    }

    public Map<String, Object> requestCollectionList() {
        Object result = this._request();
        return null != result ? (Map) result : null;
    }

    public List<Map<String, Object>> requestList() {
        return requestList(false);
    }

    public Object requestList(Boolean cache, Class clazz) {
        List<Map<String, Object>> result = requestList(cache);
        return CollectionUtils.isNotEmpty(result) ? Json.fromJson(Json.toJson(result), List.class, new Class[]{clazz}) : new LinkedList();
    }

    public Object requestList(Class clazz) {
        return requestList(false, clazz);
    }

    public List<Map<String, Object>> requestList(Boolean cached) {
        if (cached) {
            String key = this.getServiceEntry() + this.getServiceRequest();
            try {
                return (List) RestBasicServiceCache.cache.get(key, new Callable<List<Map<String, Object>>>() {
                    public List<Map<String, Object>> call() {
                        List<Map<String, Object>> r = RestBasicService.this._requestList();
                        return (List) (null == r ? new ArrayList() : r);
                    }
                });
            } catch (Exception var4) {
            }
        }
        return this._requestList();
    }

    private List<Map<String, Object>> _requestList() {
        Object result = _request();
        return null != result ? (List<Map<String, Object>>) result : null;
    }

    public Object request() {
        return request(false);
    }

    public Object request(Class clazz) {
        return request(false, clazz);
    }

    public Object request(Boolean cache, Class clazz) {
        Object object = this.request(cache);
        return Json.fromJson(Json.toJson(object), clazz);
    }

    public Object request(Boolean cached) {
        if (cached) {
            String key = this.getServiceEntry() + this.getServiceRequest();

            try {
                Object object = RestBasicServiceCache.cache.get(key, new Callable<Object>() {
                    public Object call() {
                        Object result = RestBasicService.this._request();
                        if (null == result && RestBasicService.this.checkSuccess()) {
                            result = "";
                        }

                        return result;
                    }
                });
                if ("".equals(object)) {
                    object = null;
                }

                return object;
            } catch (Exception var4) {
                var4.printStackTrace();
            }
        }

        return this._request();
    }


    private Object _request() {
        try {
            String requestUrl = StringUtils.stripEnd(this.getServiceAddress(), "/") + StringUtils.stripEnd(this.getServiceEntry(), "/") + "?token=" + this.getServiceToken();
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~requestUrl " + requestUrl);
            MultiValueMap<String, Object> headers = new LinkedMultiValueMap();
            if (null == this.getServiceRequest()) {
                this.setServiceRequest("{}");
            }
            String requestBody = this.getServiceRequest();
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~requestBody " + requestBody);
            headers.add("Accept", "application/json;charset=utf-8");
            headers.add("Content-Type", "application/json;charset=utf-8");
            HttpEntity httpEntity = new HttpEntity(requestBody, headers);
            this.serviceResult = (Map<String, Object>) restTemplate.postForEntity(requestUrl, httpEntity, LinkedHashMap.class, new Object[0]).getBody();
            if (!this.serviceResult.containsKey("code")) {
                this.serviceResult.put("code", 500);
                return null;
            }
            if (null != this.serviceResult.get("result")) {
                return this.serviceResult.get("result");
            }
        } catch (RestClientException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void setServiceRequestQuery(Object query, Object sort, Object pagination) {
        Map<String, Object> request = new HashMap<>();
        if (null != query) {
            request.put("query", query);
        }
        if (null != sort) {
            request.put("sort", sort);
        }
        if (null != pagination) {
            request.put("pagination", pagination);
        }
        this.setServiceRequest(Json.toJson(request));
    }

    public void setServiceRequestId(Object id) {
        this.setServiceRequest(id.toString());
    }

    public void setServiceRequestCreate(Object object) {
        Map result;
        if (object instanceof Map) {
            result = (Map) object;
        } else {
            result = (Map) Json.fromJson(Json.toJson(object), Map.class);
        }
        this.setServiceRequest(Json.toJson(result));
    }

    public void setServiceRequestUpdate(Object object) {
        if (object instanceof String) {
            this.setServiceRequest((String) object);
        } else {
            Map<String, Object> request = new HashMap<>();
            Map update;
            if (object instanceof Map) {
                update = (Map) object;
            } else {
                update = (Map) Json.fromJson(Json.toJson(object), Map.class);
            }
            request.put("id", update.get("id"));
            request.put("update", update);
            this.setServiceRequest(Json.toJson(request));
        }
    }

    public void setServiceRequestCreateBatch(Object object) {
        if (object instanceof String) {
            this.setServiceRequest((String) object);
        } else {
            this.setServiceRequest(Json.toJson(object));
        }
    }

    public Boolean checkSuccess() {
        return null == this.getServiceResult() || null == this.getServiceResult().get("code")
                || !ServiceResponseCode.SUCCESS.equals(this.getServiceResult().get("code"))
                && (!ServiceResponseCode.ERROR.equals(this.serviceResult.get("code")) || null == this.serviceResult.get("result")) ? false : true;
    }


    public String getServiceEntry() {
        return serviceEntry;
    }

    public void setServiceEntry(String serviceEntry) {
        this.serviceEntry = serviceEntry;
    }

    public String getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    public String getServiceToken() {
        return serviceToken;
    }

    public void setServiceToken(String serviceToken) {
        this.serviceToken = serviceToken;
    }

    public String getServiceRequest() {
        return serviceRequest;
    }

    public void setServiceRequest(String serviceRequest) {
        this.serviceRequest = serviceRequest;
    }

    public Map<String, Object> getServiceResult() {
        return serviceResult;
    }

    public void setServiceResult(Map<String, Object> serviceResult) {
        this.serviceResult = serviceResult;
    }
}
