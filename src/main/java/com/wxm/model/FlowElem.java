package com.wxm.model;

public class FlowElem {
    private String name;
    private String value;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    public FlowElem(){
        }
    public FlowElem(String name,String value){
        this.name = name;
        this.value = value;
    }
}
