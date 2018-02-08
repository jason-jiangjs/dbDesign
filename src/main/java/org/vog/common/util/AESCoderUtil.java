package org.vog.common.util;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.SecureRandom;

/**
 * AES加密解密 帮助类
 */
public class AESCoderUtil {

    private final static Logger logger = LoggerFactory.getLogger(AESCoderUtil.class);

    /**
     * 密钥算法
     */
    private static final String KEY_ALGORITHM = "AES";

    /**
     * 加密/解密算法/工作模式/填充方式
     *
     * JAVA6 支持PKCS5PADDING填充方式
     */
    private static final String CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";
    // 临时密钥
    private static byte[] _initkey = null;

    /**
     * 生成密钥，java6只支持56位密钥，bouncycastle支持64位密钥
     * 其实这里每次生成的密钥都是相同的
     * @return byte[] 二进制密钥
     */
    public static byte[] initkey() throws Exception{
        //实例化密钥生成器
        KeyGenerator kg = KeyGenerator.getInstance(KEY_ALGORITHM);
        //初始化密钥生成器，AES要求密钥长度为128位、192位、256位
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG" );
        secureRandom.setSeed(AESCoderUtil.class.getCanonicalName().getBytes());
        kg.init(128, secureRandom);
        //生成密钥
        SecretKey secretKey = kg.generateKey();
        //获取二进制密钥编码形式
        return secretKey.getEncoded();
    }

    /**
     * 转换密钥
     * @param key 二进制密钥
     * @return Key 密钥
     */
    public static Key toKey(byte[] key) throws Exception{
        //实例化DES密钥
        //生成密钥
        return new SecretKeySpec(key, KEY_ALGORITHM);
    }

    /**
     * 加密数据
     * @param data 待加密数据
     * @param key 密钥
     * @return byte[] 加密后的数据
     */
    public static byte[] encrypt(byte[] data,byte[] key) throws Exception{
        //还原密钥
        Key k=toKey(key);
        /*
         * 实例化
         * 使用 PKCS7PADDING 填充方式，按如下方式实现,就是调用bouncycastle组件实现
         * Cipher.getInstance(CIPHER_ALGORITHM,"BC")
         */
        Cipher cipher= Cipher.getInstance(CIPHER_ALGORITHM);
        //初始化，设置为加密模式
        cipher.init(Cipher.ENCRYPT_MODE, k);
        //执行操作
        return cipher.doFinal(data);
    }
    /**
     * 解密数据
     * @param data 待解密数据
     * @param key 密钥
     * @return byte[] 解密后的数据
     */
    public static byte[] decrypt(byte[] data,byte[] key) throws Exception{
        //欢迎密钥
        Key k = toKey(key);
        /*
         * 实例化
         * 使用 PKCS7PADDING 填充方式，按如下方式实现,就是调用bouncycastle组件实现
         * Cipher.getInstance(CIPHER_ALGORITHM,"BC")
         */
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        //初始化，设置为解密模式
        cipher.init(Cipher.DECRYPT_MODE, k);
        //执行操作
        return cipher.doFinal(data);
    }

    /**
     * 加密数据
     */
    public static String encode(String args) {
        try {
            if (_initkey == null) {
                _initkey = initkey();
            }
            byte[] data = AESCoderUtil.encrypt(args.getBytes("UTF-8"), _initkey);
            return Base64.encodeBase64String(data);
        } catch (Exception e) {
            logger.error("加密时出错", e);
            return "";
        }
    }

    /**
     * 解密数据
     */
    public static String decode(String args) {
        try {
            if (_initkey == null) {
                _initkey = initkey();
            }
            byte[] data = AESCoderUtil.decrypt(Base64.decodeBase64(args), _initkey);
            return new String(data, "UTF-8");
        } catch (Exception e) {
            logger.error("解密时出错", e);
            return "";
        }
    }

}
