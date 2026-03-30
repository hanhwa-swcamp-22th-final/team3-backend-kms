package com.ohgiraffers.team3backendkms.common.encryption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

@Component
public class AesEncryptor {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    private final SecretKeySpec secretKey;
    private final IvParameterSpec iv;

    public AesEncryptor(@Value("${jwt.aes.secret-key}") String base64Key) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
        this.iv = generateDeterministicIv(keyBytes);
    }

    public String encrypt(String plainText) {
        if (plainText == null) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
            byte[] encrypted = cipher.doFinal(plainText.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("AES 암호화 실패", e);
        }
    }

    public String decrypt(String cipherText) {
        if (cipherText == null) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(cipherText));
            return new String(decrypted, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("AES 복호화 실패", e);
        }
    }

    private IvParameterSpec generateDeterministicIv(byte[] keyBytes) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] hash = sha256.digest(keyBytes);
            return new IvParameterSpec(Arrays.copyOf(hash, 16));
        } catch (Exception e) {
            throw new RuntimeException("IV 생성 실패", e);
        }
    }
}
