package com.im;
import org.apache.commons.codec.binary.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * 消息加密解密工具 - 使用AES算法
 */
public class EncryptUtil {
    // 固定的加密密钥（应该从配置文件读取）
    private static final String ENCRYPT_KEY = "IM_SYSTEM_2024"; // 必须是16个字符

    private static final String ALGORITHM = "AES";

    /**
     * 加密消息
     */
    public static String encrypt(String message) {
        try {
            byte[] decodedKey = ENCRYPT_KEY.getBytes(StandardCharsets.UTF_8);
            SecretKeySpec keySpec = new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);
            
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            
            byte[] encrypted = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeBase64String(encrypted);
        } catch (Exception e) {
            System.err.println("加密错误: " + e.getMessage());
            return message; // 加密失败时返回原文
        }
    }

    /**
     * 解密消息
     */
    public static String decrypt(String encryptedMessage) {
        try {
            byte[] decodedKey = ENCRYPT_KEY.getBytes(StandardCharsets.UTF_8);
            SecretKeySpec keySpec = new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);
            
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            
            byte[] encryptedData = Base64.decodeBase64(encryptedMessage);
            byte[] decrypted = cipher.doFinal(encryptedData);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("解密错误: " + e.getMessage());
            return encryptedMessage; // 解密失败时返回原文
        }
    }

    /**
     * 生成MD5哈希（用于文件完整性验证）
     */
    public static String md5Hash(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return input;
        }
    }
}
