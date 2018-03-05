package cz.zelenikr.remotetouch.security;

import android.support.annotation.NonNull;


/**
 * Encoder / decoder to symmetric ciphering a small text data.
 *
 * @author Roman Zelenik
 */
public interface SymmetricCipher {

    /**
     * Encrypts a specific plain text.
     *
     * @param plainText text to encryption
     * @return encrypted text or null on some error
     */
    String encrypt(@NonNull String plainText);

    /**
     * Decrypts a specific data which was encrypted by this security.SymmetricCipher.
     *
     * @param encryptedText encrypted text by this security.SymmetricCipher
     * @return decrypted text or null on some error
     */
    String decrypt(@NonNull String encryptedText);
}
