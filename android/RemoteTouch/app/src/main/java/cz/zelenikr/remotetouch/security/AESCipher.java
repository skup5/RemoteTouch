package cz.zelenikr.remotetouch.security;

import android.support.annotation.NonNull;

import android.util.Base64;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import cz.zelenikr.remotetouch.security.exception.UnsupportedCipherException;
import cz.zelenikr.remotetouch.security.exception.UnsupportedKeyLengthException;

public final class AESCipher implements SymmetricCipher {

    private static final int BASE64_FLAGS = Base64.DEFAULT;
    private static final String SHA_VERSION = "SHA-256";

    private final SecretKey secretKey;
    private final Cipher cipher;

    /**
     * Initializes new AES cipher with a specific key.
     *
     * @param plainKey the given key like a plain text
     * @throws UnsupportedCipherException
     */
    public AESCipher(@NonNull String plainKey) throws UnsupportedCipherException {
        this(toSecretKey(plainKey));
    }

    /**
     * @param secretKey
     * @throws UnsupportedCipherException
     */
    private AESCipher(SecretKey secretKey) throws UnsupportedCipherException {
        this.secretKey = secretKey;
        this.cipher = initCipher();
    }

    private static Cipher initCipher() throws UnsupportedCipherException {
        try {
            return Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
            throw new UnsupportedCipherException("AES");
        }
    }

    private static SecretKey toSecretKey(String plainKey) throws UnsupportedCipherException {
        byte[] key = plainKey.getBytes();
        MessageDigest sha = null;
        try {
            sha = MessageDigest.getInstance(SHA_VERSION);
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedCipherException(e);
        }
        key = sha.digest(key);
        return new SecretKeySpec(key, "AES");
    }

    @Override
    public String encrypt(String plainData) {
        try {
            return Base64.encodeToString(encrypt(plainData.getBytes()), BASE64_FLAGS);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String decrypt(String base64EncryptedMessage) {
        try {
            return new String(decrypt(Base64.decode(base64EncryptedMessage, BASE64_FLAGS)));
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] encrypt(byte[] input) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(input);
    }

    private byte[] decrypt(byte[] input) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(input);
    }

}
