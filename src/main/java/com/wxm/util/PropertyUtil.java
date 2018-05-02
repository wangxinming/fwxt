//package com.wxm.util;
//
//import org.ho.yaml.Yaml;
//import sun.nio.ch.IOUtil;
//
//import java.io.FileInputStream;
//import java.net.URL;
//import java.util.*;
//import java.util.concurrent.locks.Lock;
//import java.util.concurrent.locks.ReentrantLock;
//
//public class PropertyUtil {
//    private static volatile  Map map;
//    private static Object object = new Object();
//    public static void main(String[] args){
//        getProperty();
//    }
//    private PropertyUtil(){}
//
//    public static Map map(){
//        if(null == map) {
//            synchronized (object) {
//                if(null == map){
//                    getProperty();
//                }
//            }
//        }
//        return map;
//    }
//
//    public static String getValue(String key){
//        if(null == map) {
//            synchronized (object) {
//                if(null == map){
//                    getProperty();
//                }
//            }
//        }
//        if(map!= null){
//            return map.get(key).toString();
//        }else{
//            return null;
//        }
//
//    }
//    /**
//     * 根据文件名读取properties文件
//     */
//    public static Map getProperty() {
//        try {
//            Yaml yaml = new Yaml();
//            URL url = PropertyUtil.class.getClassLoader().getResource("application.yml");
//            if (url != null) {
//                //application.yml文件中的配置数据，然后转换为obj，
////                Object obj =yaml.load(new FileInputStream(url.getFile()));
//                //也可以将值转换为Map
//                map =(Map)yaml.load(new FileInputStream(url.getFile()));
//                return map;
//                //通过map我们取值就可以了.
//
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return  null;
//    }
//
//}
