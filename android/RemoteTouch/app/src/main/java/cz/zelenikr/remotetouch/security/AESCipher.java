package cz.zelenikr.remotetouch.security;

import android.support.annotation.NonNull;
import android.util.Base64;

import com.google.api.client.util.Charsets;

import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import cz.zelenikr.remotetouch.security.exception.UnsupportedCipherException;

public final class AESCipher implements SymmetricCipher<String> {

    private static final String HASH_VERSION = "MD5";
    private static final int BASE64_FLAGS = Base64.DEFAULT;
    private static final Charset charset = Charsets.UTF_8;
    private static final String ALGORITHM = "AES";
    private static final String CIPHER = "AES/CBC/PKCS5Padding";

    private final Cipher cipher;
    private final IvParameterSpec initVector;
    private SecretKey secretKey;

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
        this.initVector = toInitVector("encryptionIntVec");
    }

    private static Cipher initCipher() throws UnsupportedCipherException {
        try {
            return Cipher.getInstance(CIPHER);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
            throw new UnsupportedCipherException(CIPHER);
        }
    }

    private static SecretKey toSecretKey(String plainKey) {
        final byte[] key = hashKey(plainKey.getBytes(charset));
        return new SecretKeySpec(key, ALGORITHM);
    }

    private static IvParameterSpec toInitVector(String iv) {
        return new IvParameterSpec(iv.getBytes(charset));
    }

    private static byte[] hashKey(byte[] rawKey) {
        try {
            return MessageDigest.getInstance(HASH_VERSION).digest(rawKey);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return new byte[0];
        }
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
        } catch (InvalidAlgorithmParameterException e) {
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
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean changeKey(@NonNull String plainKey) {
        this.secretKey = toSecretKey(plainKey);
        return true;
    }

    private byte[] encrypt(byte[] input) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, initVector);
        return cipher.doFinal(input);
    }

    private byte[] decrypt(byte[] input) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        cipher.init(Cipher.DECRYPT_MODE, secretKey, initVector);
        return cipher.doFinal(input);
    }

}
