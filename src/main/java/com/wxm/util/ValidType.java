package com.wxm.util;

import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

public class ValidType {
    public static boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("^[+-]?\\d+(\\.\\d+)?$");
        return pattern.matcher(str).matches();
    }
    public static boolean isDate(String str){
        boolean convertSuccess=true;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            format.setLenient(false);
            format.parse(str);
        } catch (Exception e) {
            convertSuccess=false;
        }
        return convertSuccess;
    }

    public static void main(String[] args){
        boolean ret = ValidType.isDate("1987-12-14");
        ret = ValidType.isDate("1987-12-34");
        ret = ValidType.isDate("1987-13-14");
        ret = ValidType.isDate("87-12-14");
        int i = 0;
    }
}
