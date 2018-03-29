package com.wxm.controller;

import com.wxm.util.HtmlProcess;
import com.wxm.util.Word2Html;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

@Controller
public class WordTemplateController {
    @RequestMapping(value="/batchImport",method= RequestMethod.POST)
    public Object saveThingsParse(MultipartFile file, HttpServletRequest request, HttpServletResponse response) {
        try {
            File desFile = new File("F:\\tmp\\oa\\demo.doc");
            if(!desFile.getParentFile().exists()){
                desFile.mkdirs();
            }
            file.transferTo(desFile);

            File htmlFile = Word2Html.office2pdf("F:\\tmp\\oa\\demo.doc");

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
            String htmlStr = htmlSb.toString();
            HtmlProcess.clearFormat(htmlStr,"F:\\tmp\\images");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }
}
