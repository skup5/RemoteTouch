package cz.zelenikr.remotetouch.security;

import android.support.annotation.NonNull;

import android.util.Base64;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class AESCipher implements SymmetricCipher {

    private static final int KEY_BITS_LENGTH = 256; // 192 and 256 bits may not be available
    private static final int BASE64_FLAGS = Base64.DEFAULT;
    private static final String SHA_VERSION = "SHA-256";

    private final SecretKey secretKey;
    private final Cipher cipher;

    /**
     * Initializes new AES cipher with random key.
     *
     * @throws UnsupportedKeyLengthException
     * @throws UnsupportedCipherException
     */
    public AESCipher() throws UnsupportedKeyLengthException, UnsupportedCipherException {
        try {
            this.secretKey = generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedKeyLengthException(KEY_BITS_LENGTH + "");
        }

        this.cipher = initCipher();
    }

    /**
     * Initializes new AES cipher with a specific key.
     *
     * @param plainKey the given key like a plain text
     * @throws UnsupportedCipherException
     * @throws NoSuchAlgorithmException
     */
    public AESCipher(@NonNull String plainKey) throws UnsupportedCipherException, NoSuchAlgorithmException {
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

    /**
     * Generates new random key for AES cipher and returns it.
     *
     * @return new random key or null if some error occurred
     */
    public static byte[] generateAESKey() {
        try {
            return generateKey().getEncoded();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Generates new random key for AES cipher and returns it like a plain text.
     *
     * @return new random key or null if some error occurred
     */
    public static String generatePlainAESKey() {
        try {
            return generateKey().getEncoded().toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Cipher initCipher() throws UnsupportedCipherException {
        try {
            return Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
            throw new UnsupportedCipherException("AES");
        }
    }

    private static SecretKey toSecretKey(String plainKey) throws NoSuchAlgorithmException {
        byte[] key = plainKey.getBytes();
        MessageDigest sha = MessageDigest.getInstance(SHA_VERSION);
        key = sha.digest(key);
        return new SecretKeySpec(key, "AES");
    }

    private static SecretKey generateKey() throws NoSuchAlgorithmException {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(KEY_BITS_LENGTH);
        return kgen.generateKey();
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


    public static class UnsupportedKeyLengthException extends RuntimeException {
        public UnsupportedKeyLengthException(String message) {
            super(message);
        }
    }

    public static class UnsupportedCipherException extends RuntimeException {
        public UnsupportedCipherException(String message) {
            super(message);
        }
    }
}
