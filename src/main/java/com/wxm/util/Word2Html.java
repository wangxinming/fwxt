package com.wxm.util;

import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeManager;

import java.io.File;

public class Word2Html {
    /**
     * 使Office2003-2007全部格式的文档(.doc|.docx|.xls|.xlsx|.ppt|.pptx) 转化为html文件
     * @param inputFilePath 源文件路径,如："D:/论坛.docx"
     * @return
     */
    public static File openOfficeToPDF(String inputFilePath,String path) {
        return office2pdf(inputFilePath,path);
    }

    /**
     * 根据操作系统的名称，获取OpenOffice.org 4的安装目录<br>
     * 如我的OpenOffice.org 4安装在：C:/Program Files (x86)/OpenOffice 4
     * @return OpenOffice.org 4的安装目录
     */
//    public static String getOfficeHome(String path) {
//
//        //这是返回的是OpenOffice的安装目录,建议将这个路径加入到配置文件中,然后直接通过配置文件获取
//        //我这里就直接写死了
//        return PropertyUtil.getValue("openoffice.org.path");
//    }

    /**
     * 连接OpenOffice.org 并且启动OpenOffice.org
     * @return
     */
    public static OfficeManager getOfficeManager(String path) {
        DefaultOfficeManagerConfiguration config = new DefaultOfficeManagerConfiguration();

        // 设置OpenOffice.org 4的安装目录
        config.setOfficeHome(path);

        // 启动OpenOffice的服务
        OfficeManager officeManager = config.buildOfficeManager();
        officeManager.start();

        return officeManager;
    }

    /**
     * 转换文件
     * @param inputFile
     * @param outputFilePath_end
     * @param inputFilePath
     * @param converter
     */
    public static File converterFile(File inputFile,String outputFilePath_end,String inputFilePath,
                                     OfficeDocumentConverter converter) {

        File outputFile = new File(outputFilePath_end);

        //判断目标路径是否存在,如不存在则创建该路径
        if (!outputFile.getParentFile().exists()){
            outputFile.getParentFile().mkdirs();
        }
        converter.convert(inputFile, outputFile);//转换

        System.out.println("文件:"+inputFilePath+"\n转换为\n目标文件:"+outputFile+"\n成功!");

        return outputFile;
    }
//    public static File office2pdf(File inputFile) {
//        OfficeManager officeManager = null;
//        try {
////            if (inputFilePath==null||inputFilePath.trim().length()<=0) {
////                System.out.println("输入文件地址为空，转换终止!");
////                return null;
////            }
////            File inputFile = new File(inputFilePath);
////
////            //转换后的文件路径
////            String outputFilePath_end=getOutputFilePath(inputFilePath);
//
//            if (!inputFile.exists()) {
//                System.out.println("输入文件不存在，转换终止!");
//                return null;
//            }
//
//            //获取OpenOffice的安装路劲
//            officeManager = getOfficeManager();
//
//            //连接OpenOffice
//            OfficeDocumentConverter converter=new OfficeDocumentConverter(officeManager);
//
//            //转换并返回转换后的文件对象
//            return converterFile(inputFile,outputFilePath_end,inputFilePath,converter);
//
//        } catch (Exception e) {
//            System.out.println("转化出错!");
//            e.printStackTrace();
//        } finally {
//
//            if (officeManager != null) {
//
//                //停止openOffice
//                officeManager.stop();
//            }
//        }
//        return null;
//    }
    public static File html2pdf(String inputFilePath,String path) {
        OfficeManager officeManager = null;
        try {
            if (inputFilePath==null||inputFilePath.trim().length()<=0) {
                System.out.println("输入文件地址为空，转换终止!");
                return null;
            }
            File inputFile = new File(inputFilePath);

            //转换后的文件路径
            String outputFilePath_end=getOutputFilePath(inputFilePath,".pdf");

            if (!inputFile.exists()) {
                System.out.println("输入文件不存在，转换终止!");
                return null;
            }

            //获取OpenOffice的安装路劲
            officeManager = getOfficeManager(path);

            //连接OpenOffice
            OfficeDocumentConverter converter=new OfficeDocumentConverter(officeManager);

            //转换并返回转换后的文件对象
            return converterFile(inputFile,outputFilePath_end,inputFilePath,converter);

        } catch (Exception e) {
            System.out.println("转化出错!");
            e.printStackTrace();
        } finally {

            if (officeManager != null) {

                //停止openOffice
                officeManager.stop();
            }
        }
        return null;
    }
    /**
     * 使Office2003-2007全部格式的文档(.doc|.docx|.xls|.xlsx|.ppt|.pptx) 转化为html文件
     * @param inputFilePath 源文件路径，如："D:/论坛.docx"
     * @return
     */
    public static File office2pdf(String inputFilePath,String path) {
        OfficeManager officeManager = null;
        try {
            if (inputFilePath==null||inputFilePath.trim().length()<=0) {
                System.out.println("输入文件地址为空，转换终止!");
                return null;
            }
            File inputFile = new File(inputFilePath);

            //转换后的文件路径
            String outputFilePath_end=getOutputFilePath(inputFilePath,".html");

            if (!inputFile.exists()) {
                System.out.println("输入文件不存在，转换终止!");
                return null;
            }

            //获取OpenOffice的安装路劲
            officeManager = getOfficeManager(path);

            //连接OpenOffice
            OfficeDocumentConverter converter=new OfficeDocumentConverter(officeManager);

            //转换并返回转换后的文件对象
            return converterFile(inputFile,outputFilePath_end,inputFilePath,converter);

        } catch (Exception e) {
            System.out.println("转化出错!");
            e.printStackTrace();
        } finally {

            if (officeManager != null) {

                //停止openOffice
                officeManager.stop();
            }
        }
        return null;
    }

    /**
     * 获取输出文件
     * @param inputFilePath
     * @return
     */
    public static String getOutputFilePath(String inputFilePath,String regex) {
        String outputFilePath=inputFilePath.replaceAll("."+getPostfix(inputFilePath),regex);
        return outputFilePath;
    }

    /**
     * 获取inputFilePath的后缀名,如:"D:/论坛.docx"的后缀名为:"docx"
     * @param inputFilePath
     * @return
     */
    public static String getPostfix(String inputFilePath) {
        return inputFilePath.substring(inputFilePath.lastIndexOf(".") + 1);
    }

    //测试
    public static void main(String[] args) {
        File html = openOfficeToPDF("F:\\tmp\\服务合同模板201803.doc","C:/Program Files (x86)/OpenOffice 4");
        int tt = 0;
    }
}
