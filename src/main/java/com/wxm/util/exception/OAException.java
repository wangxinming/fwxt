package com.wxm.util.exception;

public class OAException extends RuntimeException{
    private int errorCode;

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public OAException(int errorCode,String message){
        super(message);
        this.errorCode = errorCode;
    }

    public OAException(int errorCode,String message,Exception ex){
        super(message,ex);
        this.errorCode = errorCode;
    }
}