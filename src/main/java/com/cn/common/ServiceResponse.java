package com.cn.common;

public class ServiceResponse {
    public ServiceResponse() {
        this.code = ServiceResponseCode.SUCCESS;
        this.description = "success";
    }

    private Integer code;
    private Object result;
    private String description;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
