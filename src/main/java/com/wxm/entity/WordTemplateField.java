package com.wxm.entity;

public class WordTemplateField {
    private int id;
    private int templateId;
    private String name;
    private String field;
    private String fieldMd5;
    private String type;
    private String length;
    private String start;
    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return name;
    }
    public void setId(int id){
        this.id = id;
    }
    public int getId(){
        return id;
    }
    public void setTemplateId(int templateId){
        this.templateId = templateId;
    }
    public int getTemplateId(){
        return templateId;
    }
    public void setField(String field){
        this.field = field;
    }
    public String getField(){
        return field;
    }
    public void setFieldMd5(String fieldMd5){
        this.fieldMd5 = fieldMd5;
    }
    public String getFieldMd5(){
        return fieldMd5;
    }
    public void setType(String type){
        this.type = type;
    }
    public String getType(){
        return type;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getLength() {
        return length;
    }

    public void setStart(String start) {
        this.start = start;
    }
    public String getStart(){
        return start;
    }
}
