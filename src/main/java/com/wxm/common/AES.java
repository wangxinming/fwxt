package com.wxm.common;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.*;

public class AES {
    public static boolean initialized = false;

    /**
     * AES解密
     * @param content 密文
     * @return
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchProviderException
     */
    public static byte[] decrypt(byte[] content, byte[] keyByte, byte[] ivByte) throws InvalidAlgorithmParameterException {
        initialize();
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            Key sKeySpec = new SecretKeySpec(keyByte, "AES");

            cipher.init(Cipher.DECRYPT_MODE, sKeySpec, generateIV(ivByte));// 初始化
            byte[] result = cipher.doFinal(content);
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static void initialize(){
        if (initialized) return;
        Security.addProvider(new BouncyCastleProvider());
        initialized = true;
    }
    //生成iv
    public static AlgorithmParameters generateIV(byte[] iv) throws Exception{
        AlgorithmParameters params = AlgorithmParameters.getInstance("AES");
        params.init(new IvParameterSpec(iv));
        return params;
    }
    public static void main(String[] args){
        String encryptedData = "W7kKXwGQVxjxO78do14mpdtZ3BB1Kgl6zcvr1xTmcpWXWyJFm/LEr3wq9cNaiosPbQ8dYdc1BVgtYjj6ZXtAF4GoUhKWvuKcc6oyCTb4vg0fd3QH0kymD67TKrv6iAZjQe2oGLkQdDSBTDxJ52aZ369yu2Zraf20u+wNub5nooz5UDSScCx6kiggLaY/z00umLBS94+du5nb/4hXxYRuUAO/JpqKQQOzLHeFbV6/B9LJ0FLGkHoFy+ic4EGVZUaZ/IXkIiq42NiZ/jb6kZKV2OULOj1iG2Hsd4gbngMgXJtu1JSgMClb3L73eOs6F+JjMGKL5CLQYB8of3ePvMIxRggQJf+hWXpxv/9t2bOfCuEVG1v5e/TOQXktqUqtHLmM+v9M38yDs+w8L9nwb2bikJAZXpe9u3BEa7nxS/BThC71upJgMkyNN7TurqNmMl+OQVAFeVzng/Te3P5vnzj9HZGSbYtyYl1RCIfxbEURizMkWv7lkIvx3TVnkL/HYeVZKdgsExOgRHD/LWtDfRZpacL4Zjv3q69K6Za9dBY13SzmdOulTW0nkVXI4Wdo6HghsnchXN4hSrVP2bAajS8ojsgEmMRFahWlLoPPL6RMPE78/bxYVI758AdcRc4s/BAL7NvgOr6dTm9/ANxPHd/dXAostlFvD3qVmWDDdS+d3jF/JFu5Cegx7inLDeL0ulCGwH+uN39rOuPCHw0R+tEApyayfb3Z+6kj0Rdh1jQdNaqBZCOwIo21EAapJ1F0RXtMLutWYUkdf6oFRP5hjx+kQs3Aen9G+m/jDi++g1usDD3Ew56zneX7c0sqvWuRrTWKpOLY4nfp8a4ADjsOw8isc/sM+WIlloAnzvHb/FfTPgnmaxyzgwyc2YsGE46ALsn/CBE8IrnA6XhCO/IrNzPPMxEavanajldh9Q2K9rlhE9SgTKrZFuLk50zg9i7R1xj/wy2e6MmRjnL7j2aHz3drzuVsfb3kKG0/DnXMyLF2xb3mN9qYnPIleANlvoby//mFSNdBgXqkZSQxS1Pr+UfkqtZ4UUMjU+bFKoU4UiP3fudn78veu+ah7+6VIftFTREWjuY/itutpX8Y662SkxAraaEWXRih6tTdKtV1rH1w5v6xi+owIOF+2bnki8tzcR4y8KR8tHNIuwj1eA0KAgI3mjLuMi8zN4jiMYW1aqa9JImA0P/KYzc8lpL0Obp+5NA9e83GVrl6bEUX++R+YJilmi5PeAi4jkzGo+PB1CikMAArhKwUz/6oYZEA97X588j1hd34BxWYGt48iJkrFCv6RmmqgsuU4o/fqci2fLHFcDm4saGvlRCgaau0sSYzqu+syZFcZ7qX0Lm7Z+9m46NnKANSzW0NisISziLfPXo2/qW8W5MtHrIhjyXk5sUu2+DDFwGRe7tTeiPej7Gv0EM3yrJjWgyDBB3NblESmsB90aL+Z3z8y280w7te2lWQ/nFFhMGCMd40TIPtJgP5B690BrOHtQiHNdvM6gdDQ97s4IYReVtH7yCEkXI/eOKUlmawdkhJ5D17H/HWScAQ+yjD1kRU17ptvPftELrSX1aUoaiQuYnL7xEiOrLrsA0cTTDdRjYbaFp4OeItDd09TgB91GrnhrBvDW/G0BS90vPNnC9rVj05rkEdHXT7oCGQtXpZ7OujNkAz5Y4wGSf7S23T3g==";
        String session_key = "061OGJPe0uOQTB1lhLSe0RhMPe0OGJPW";
        String iv = "1AylqYZ8Wvva08mSANCXSw==";
        byte[] resultByte  = new byte[0];
        try {
            resultByte = AES.decrypt(Base64.decodeBase64(encryptedData),
                    Base64.decodeBase64(session_key),
                    Base64.decodeBase64(iv));
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

        try {
            String userInfo = new String(resultByte, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
