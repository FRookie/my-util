package org.example.common.util.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

/**
 * @date 2021/5/13 14:25
 */
public class AESUtils {
    private static final Logger logger = LoggerFactory.getLogger(AESUtils.class);

    // 密钥算法
    private static final String KEY_ALGORITHM = "AES";
    // 加解密算法/工作模式/填充方式,Java6.0支持PKCS5Padding填充方式,BouncyCastle支持PKCS7Padding填充方式
    private static final String CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";
    // 默认key
    public static final String KEY = "bjjlybhulrpQMSuxkZ9SgOyAZjrQ+Iv67l3bUz9hzcs=";
    public static final String IOS_KEY = "VnEybnZLOWRaYWo3dmprTA==";

    /**
     * 加密数据
     * @param data 待加密数据
     * @param key 密钥
     * @return 加密后的数据
     * */
    public static String encrypt(String data, String key) {
        String str = null;
        try {
            // 还原密钥
            Key k = new SecretKeySpec(Base64.getDecoder().decode(key), KEY_ALGORITHM);
            // 实例化Cipher对象，它用于完成实际的加密操作
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            // 初始化Cipher对象，设置为加密模式
            cipher.init(Cipher.ENCRYPT_MODE, k);
            // 执行加密操作。加密后的结果通常都会用Base64编码进行传输
            str = Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
        } catch (Exception e) {
            logger.error("encrypt fail, data:{}, key:{}", data, key, e);
        }
        return str;
    }

    /**
     * 解密数据
     * @param data 待解密数据
     * @param key 密钥
     * @return 解密后的数据
     * */
    public static String decrypt(String data, String key) {
        String str = null;
        try {
            Key k = new SecretKeySpec(Base64.getDecoder().decode(key), KEY_ALGORITHM);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            // 初始化Cipher对象，设置为解密模式
            cipher.init(Cipher.DECRYPT_MODE, k);
            // 执行解密操作
            str = new String(cipher.doFinal(Base64.getDecoder().decode(data.replace("\n",""))));
        } catch (Exception e) {
            logger.error("decrypt fail, data:{}, key:{}", data, key, e);
        }
        return str;
    }

    /**
     * 加密数据（使用默认密钥）
     * @param data 待加密数据
     * @return 加密后的数据
     * */
    public static String encrypt(String data) {
        return encrypt(data, KEY);
    }

    /**
     * 解密数据（使用默认密钥）
     * @param data 待解密数据
     * @return 解密后的数据
     * */
    public static String decrypt(String data) {
        return decrypt(data, KEY);
    }

}
