//package com.wxm.util;
//
//import java.net.InetAddress;
//import java.net.NetworkInterface;
//import java.net.SocketException;
//import java.net.UnknownHostException;
//
//
//public class UniqueIdGenerator {
//    private static boolean     isLinuxPlatform       = false;
//    public static final String OS_NAME               = System.getProperty("os.name");
//    static {
//        if (OS_NAME != null && OS_NAME.toLowerCase().indexOf("linux") >= 0) {
//            isLinuxPlatform = true;
//        }
//    }
//    private static boolean isLinuxPlatform() {
//        return isLinuxPlatform;
//    }
//
//    public String getDatacenterId() {
//        try {
//            String id="";
//            NetworkInterface network = null;
//            if (isLinuxPlatform()) {
//                network = NetworkInterface.getByName("eth0");
//            } else {
//                InetAddress ip = InetAddress.getLocalHost();
//                network = NetworkInterface.getByInetAddress(ip);
//            }
//            if (null == network) {
//                return getRandomNum();
//            }
//            byte[] mac = network.getHardwareAddress();
//            if (null == mac || mac.length == 0) {
//                return getRandomNum();
//            }
//            long id = ((0x000000FF & (long) mac[mac.length - 1]) | (0x0000FF00 & (((long) mac[mac.length - 2]) << 8))) >> 6;
//            return id;
//        } catch (SocketException e) {
//
//        } catch (UnknownHostException e) {
//
//        }
//    }
//}
