//package com.wxm.util;
//
//import com.wxm.entity.MailConfig;
//import org.apache.commons.lang3.StringUtils;
//
//import java.util.List;
//import java.util.Map;
//
//public class MailUtil {
//    public static MailConfig setConfig(String smtp,String port,String email,String emailName,String userName,String password){
//        MailConfig mc=new MailConfig(smtp,port,email,emailName,userName,password);
//        return mc;
//    }
//    /**
//     * 设置邮箱配置获取邮箱配置类
//     * @param mailProp 邮箱配置位置
//     * @return
//     */
//    public static MailConfig setConfig(String mailProp){
//        Map map = PropertyUtil.getProperty();
//        MailConfig mc=new MailConfig("smtp.163.com","25","wangxinming1000@163.com","wangxinming1000@163.com","wangxinming1000@163.com","aszcM146");
//        return mc;
//    }
//
//    /**
//     * 发送邮件并发送附件
//     * @param toMail        收件人地址
//     * @param subject       发送主题
//     * @param content       发送内容
//     * @param files         附件列表
//     * @throws Exception
//     * @return              成功返回true，失败返回false
//     */
//    public static boolean send(MailConfig mc,String toMail, String subject, String content, List<String> files){
//        return sendProcess(mc,toMail,null,null,subject, content, files);
//    }
//    /**
//     * 发送邮件
//     * @param ccAdress    抄送人地址
//     * @param subject     发送主题
//     * @param content     发送内容
//     * @throws Exception
//     */
//    public static boolean sendProcess(MailConfig mc,String toMailList,String ccAdress,String bccAdress,String subject, String content,List<String> fileList){
//        try{
//            EmailHandle emailHandle = new EmailHandle(mc.getSmtp(),mc.getPort());
//            emailHandle.setFrom(mc.getEmail(),mc.getEmailName());
//            emailHandle.setNeedAuth(true);
//            emailHandle.setSubject(subject);
//            emailHandle.setBody(content);
//            emailHandle.setToList(toMailList);
//            /**添加抄送**/
//            if(StringUtils.isNotEmpty(ccAdress)){
//                emailHandle.setCopyToList(ccAdress);
//            }
//            /**添加暗送**/
//            if(StringUtils.isNotEmpty(bccAdress)){
//                emailHandle.setBlindCopyToList(bccAdress);
//            }
//            emailHandle.setNamePass(mc.getUserName(),mc.getPassword());
//            if(null != fileList && fileList.size() > 0){
//                /** 附件文件路径 **/
//                for(String file : fileList){
//                    emailHandle.addFileAffix(file);
//                }
//            }
//            return emailHandle.sendEmail();
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//        return false;
//    }
//}
