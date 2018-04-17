package com.wxm.util;

import java.util.regex.Pattern;

public class ValidType {
    public static boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }
}
