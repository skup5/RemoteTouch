package cz.zelenikr.remotetouch.security;

import android.support.annotation.NonNull;
import android.util.Base64;

import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import cz.zelenikr.remotetouch.security.exception.UnsupportedCipherException;

public final class AESCipher implements SymmetricCipher<String> {

    private static final int BASE64_FLAGS = Base64.DEFAULT;
    private static final String
        HASH_VERSION = "SHA-1",
        DEF_CHARSET = "UTF-8";
    private static final Charset charset = Charset.forName(DEF_CHARSET);

    private  SecretKey secretKey;
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

    private static SecretKey toSecretKey(String plainKey) {
        byte[] key = plainKey.getBytes(charset);
        key = hashKey(key);
        return new SecretKeySpec(key, "AES");
    }

    private static byte[] hashKey(byte[] rawKey) {
        MessageDigest sha = null;
        try {
            sha = MessageDigest.getInstance(HASH_VERSION);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] key = sha.digest(rawKey);
        key = Arrays.copyOf(key, 16); // use only first 128 bit
        return key;
    }

    @Override
    public String encrypt(@NonNull String plainData) {
        try {
            return Base64.encodeToString(encrypt(plainData.getBytes(charset)), BASE64_FLAGS);
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
    public String decrypt(@NonNull String base64EncryptedMessage) {
        try {
            return new String(decrypt(Base64.decode(base64EncryptedMessage, BASE64_FLAGS)), charset);
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean changeKey(@NonNull String plainKey){
        this.secretKey = toSecretKey(plainKey);
        return true;
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
