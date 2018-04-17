package com.wxm.entity;

public class KeyValue {
    private String key;
    private String value;
    public void setKey(String key){
        this.key = key;
    }
    public String getKey(){
        return key;
    }
    public void setValue(String value){
        this.value = value;
    }
    public String getValue(){
        return value;
    }
    public KeyValue(){}
    public KeyValue(String key,String value){
        this.key = key;
        this.value = value;
    }
}
