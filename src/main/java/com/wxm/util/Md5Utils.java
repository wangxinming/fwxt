package com.wxm.util;

import java.security.MessageDigest;
public abstract class Md5Utils {
    public static String getMd5(String str){
        try {
            char hexDigits[] = {
                    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
            };
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            byte[] mb = md.digest();
            int j = mb.length;
            char result[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte bt = mb[i];
                result[k++] = hexDigits[bt >>> 4 & 0xf];
                result[k++] = hexDigits[bt & 0xf];
            }
            return new String(result).toUpperCase();
        }catch (Exception e){}
        return null;
    }
}
