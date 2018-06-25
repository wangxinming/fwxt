package com.wxm.util;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlProcess {
    /**
     * 清除一些不需要的html标记
     *
     * @param htmlStr
     *                带有复杂html标记的html语句
     * @return 去除了不需要html标记的语句
     */
    public static String clearFormat(String htmlStr, String docImgPath) {
        // 获取body内容的正则
        String bodyReg = "<BODY .*</BODY>";
        Pattern bodyPattern = Pattern.compile(bodyReg);
        Matcher bodyMatcher = bodyPattern.matcher(htmlStr);
        if (bodyMatcher.find()) {
            // 获取BODY内容，并转化BODY标签为DIV
            htmlStr = bodyMatcher.group().replaceFirst("<BODY", "<DIV")
                    .replaceAll("</BODY>", "</DIV>");
        }
        // 调整图片地址
        htmlStr = htmlStr.replaceAll("<IMG SRC=\"", "<IMG SRC=\"" + docImgPath + "/");
        htmlStr = htmlStr.replaceAll("<img[^>]*>", " ");
        htmlStr = htmlStr.replaceAll("<IMG[^>]*>", " ");
        // 把<P></P>转换成</div></div>保留样式
        htmlStr = htmlStr.replaceAll("(<P)([^>]*>.*?)(<\\/P>)", "<DIV$2</DIV>");
        // 把<P></P>转换成</div></div>并删除样式
//        htmlStr = htmlStr.replaceAll("(<P)([^>]*)(>.*?)(<\\/P>)", "<div$3</div>");
        // 删除不需要的标签
        htmlStr = htmlStr
                .replaceAll(
                        "<[/]?(font|FONT|span|SPAN|xml|XML|del|DEL|ins|INS|meta|META|[ovwxpOVWXP]:\\w+)[^>]*?>",
                        "");
        // 删除不需要的属性
        htmlStr = htmlStr
                .replaceAll(
                        "<([^>]*)(?:lang|LANG|class|CLASS|style|STYLE|size|SIZE|face|FACE|[ovwxpOVWXP]:\\w+)=(?:'[^']*'|\"\"[^\"\"]*\"\"|[^>]+)([^>]*)>",
                        "<$1$2>");

        // 删除</U><U>
        htmlStr = htmlStr.replaceAll("<U>","");
        htmlStr = htmlStr.replaceAll("</U>","");
        return htmlStr;
    }
    public static void main(String[] args){
        File htmlFile = new File("F:\\tmp\\服务合同模板201803.html");
        // 获取html文件流
        StringBuilder htmlSb = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(htmlFile),"GBK"));
            while (br.ready()) {
                htmlSb.append(br.readLine());
            }
            br.close();
            // 删除临时文件
//            htmlFile.delete();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // HTML文件字符串
        String htmlStr = htmlSb.toString().toUpperCase();
        clearFormat(htmlStr,"F:\\tmp\\oa\\images");
    }
}
