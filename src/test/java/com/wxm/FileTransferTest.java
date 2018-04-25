package com.wxm;

import com.wxm.util.FileByte;
import org.junit.Test;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileTransferTest {
    @Test
    public void testFile(){

        byte[] streams = FileByte.getByte("F:\\tmp\\oa\\请假申请-2018-04-18.pdf");

        File file = new File("F:\\tmp\\oa\\请假申请-2018-04-18bak.pdf");
        try {
            PrintStream ps = new PrintStream(new FileOutputStream(file));
            try {
                ps.write(streams);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void TestReplace(){
        StringBuilder htmlStr = new StringBuilder("<html><head><title>请假申请-2018-04-23</title><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" /><body><div><div class=\"cjk\" align=\"CENTER\"><b>电脑及网络维护服务协议</b></div><div class=\"cjk\" align=\"LEFT\">　　</div><div class=\"cjk\" align=\"LEFT\">甲方：<u>fuidshfuihduihgui</u></div><div class=\"cjk\" align=\"LEFT\">乙方：<u><input type=\"text\" style=\"border:none;border-bottom:1px solid #000;\" name=\"name_A106771F53E0B9A8823AB924D4F3492B\" value=\"44444\" id=\"name_A106771F53E0B9A8823AB924D4F3492B\"></u></div><div class=\"cjk\" align=\"LEFT\">　　甲、乙双方就甲方的工作电脑及网络维护服务相关事宜，经双方共同协商，达成以下协议：</div><div class=\"cjk\" align=\"LEFT\"><b>一、服务内容</b></div><div class=\"cjk\" align=\"LEFT\">1、乙方根据甲方现有的电脑软件系统提出评估报告或修改建议。对电脑软件网络系统规划提供建设方案，此方案包括：电脑系统软件规划的方案、网络软件规划的方案、软件应用的方案、系统软件安全的方案等。</div><div class=\"cjk\" align=\"LEFT\">2、乙方负责对甲方的电脑进行每年至少<u><input type=\"text\" style=\"border:none;border-bottom:1px solid #000;\" name=\"name_8019D8F26796C3DBD6DF4EFDAE57AEDE\" value=\"1\" id=\"name_8019D8F26796C3DBD6DF4EFDAE57AEDE\"></u>次病毒防杀及常用软件的维护巡查（不含专业用软件）。</div><div class=\"cjk\" align=\"LEFT\">3、乙方向甲方提供每季度不超过<u><input type=\"text\" style=\"border:none;border-bottom:1px solid #000;\" name=\"name_904E96EA5834A8AC6A2B3626EF4ADBC5\" value=\"11\" id=\"name_904E96EA5834A8AC6A2B3626EF4ADBC5\"></u>次的上门维护服务（以甲方报修次数为准），保证电脑系统和软件的正常运行，若超过次数则另行协商按次收费。</div><div class=\"cjk\" align=\"LEFT\">4、乙方接到甲方报修电话后，最晚于次日到达维护。</div><div class=\"cjk\" align=\"LEFT\">5、乙方负责为甲方提供网络技术服务，保障甲方网络正常运行（包括局域网及INTERNET网，ISP和政务网络问题除外）。</div><div class=\"cjk\" align=\"LEFT\"><b>二、服务期限</b></div><div class=\"cjk\" align=\"LEFT\">本协议自甲乙双方签字之日起生效，至___<input type=\"text\" style=\"border:none;border-bottom:1px solid #000;\" name=\"name_80AD8F02D91B5E32929B8B825AE0A182\" id=\"name_80AD8F02D91B5E32929B8B825AE0A182\">_日终结。</div><div class=\"cjk\" align=\"LEFT\"><b>三、服务价款</b></div><div class=\"cjk\" align=\"LEFT\">本协议服务费按照每月<u> <input type=\"text\" style=\"border:none;border-bottom:1px solid #000;\" name=\"name_DD3E21B6390E9F6BFAABECA55165521A\" id=\"name_DD3E21B6390E9F6BFAABECA55165521A\"> </u>元，共计人民币（大写）：<u><input type=\"text\" style=\"border:none;border-bottom:1px solid #000;\" name=\"name_3B11A2A66D161E18FB6AA93EA8E5C47B\" id=\"name_3B11A2A66D161E18FB6AA93EA8E5C47B\"></u>，￥<u><input type=\"text\" style=\"border:none;border-bottom:1px solid #000;\" name=\"name_3B21011687CE8F46E9909F87FC799AD1\" id=\"name_3B21011687CE8F46E9909F87FC799AD1\"></u>元。</div><div class=\"cjk\" align=\"LEFT\">合同签署一周后，甲方根据乙方出具的发票，全额支付。</div><div class=\"cjk\" align=\"LEFT\"><b>四、争议的解决</b></div><div class=\"cjk\" align=\"LEFT\">对本协议未尽事宜或在执行过程中发生的争议，双方应本着友好合作精神共同协商解决。</div><div class=\"cjk\" align=\"LEFT\">五、协议到期前<u><input type=\"text\" style=\"border:none;border-bottom:1px solid #000;\" name=\"name_3C4DD864BB1AC05CEC995C48ECDACB13\" id=\"name_3C4DD864BB1AC05CEC995C48ECDACB13\"></u>个工作日，如甲、乙双方任意一方不愿继续续约，需书面向另一方提出，否则此协议将自动延续至下一年度。</div><div class=\"cjk\" align=\"LEFT\">本协议一式三份，甲方执两份，乙方一份，具有同等法律效力。</div><div class=\"cjk\" align=\"LEFT\"><br></div><div class=\"cjk\" align=\"LEFT\"><br></div><div class=\"cjk\" align=\"LEFT\"><br></div><div class=\"cjk\" align=\"LEFT\"><br></div><div class=\"cjk\" align=\"LEFT\">甲方（盖章）：_________　　　乙方（盖章）：_________　　<br>　　负责人（签字）：_________　　　负责人（签字）：_________　　<br>　　_________年____月____日　　_________年____月____日</div><div type=\"FOOTER\">\t<div align=\"CENTER\"> <sdfield type=\"PAGE\" subtype=\"RANDOM\" format=\"PAGE\">2</sdfield>\t/\t<b><sdfield type=\"DOCSTAT\" subtype=\"PAGE\" format=\"ARABIC\">2</sdfield></b></div>\t<div align=\"LEFT\"><br>\t</div></div></div></body></html>\n");
        Pattern pattern = Pattern.compile("<input([\\s\\S]*?)>");
        Matcher matcher = pattern.matcher(htmlStr);
        Map<String,String> map = new LinkedHashMap<>();
        while(matcher.find()) {
            String tmp = matcher.group();
            String name = tmp.substring(tmp.indexOf("id=")+4,tmp.indexOf(">")-1);
            map.put(name,tmp);

//            htmlStr.replace(start,start+tmp.length(),"hello");
//            htmlStr.replace(matcher.start(),matcher.end(),"hello");

        }
//        for(String str : map.){
//            int start = htmlStr.indexOf(str);
//            htmlStr.replace(start,start+str.length(),"hello");
//        }

        int i = 0;
    }

}
