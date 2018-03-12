package cz.zelenikr.remotetouch.security;

import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * Simple generator of AES plain text keys.
 *
 * @author Roman Zelenik
 */
public class AESKeyGenerator implements SymmetricKeyGenerator<String> {

    public enum KeyLength { // 192 and 256 bits may not be available
        Bits256, Bits192, Bits128;

        int toLength() {
            switch (this) {
                case Bits128:
                    return 128;
                case Bits192:
                    return 192;
                case Bits256:
                    return 256;
                default:
                    return 0;
            }
        }
    }

    private static final KeyLength DEFAULT_KEY_BITS_LENGTH = KeyLength.Bits256;

    private final int keyBitsLength;

    /**
     * Initialize new key generator with {@link #DEFAULT_KEY_BITS_LENGTH}.
     */
    public AESKeyGenerator() {
        this(DEFAULT_KEY_BITS_LENGTH);
    }

    /**
     * Initialize new key generator with specific key length.
     *
     * @param bits required length (in bits)
     */
    public AESKeyGenerator(KeyLength bits) {
        this.keyBitsLength = bits.toLength();
    }

    @Override
    public String generate() {
        return generatePlainKey(keyBitsLength);
    }

    /**
     * Generates new random {@link SecretKey} with specific length of bits
     * for AES cipher and returns it.
     *
     * @param keyBitsLength
     * @return new random key or null if some error occurred
     * @throws NoSuchAlgorithmException Throws if AES is not supported on this platform
     */
    private static SecretKey generateSecretKey(int keyBitsLength) throws NoSuchAlgorithmException {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(keyBitsLength);
        return kgen.generateKey();
    }

    /**
     * Generates new random key with specific length of bits for AES cipher and returns it.
     *
     * @return new random key or null if some error occurred
     */
    private static byte[] generateByteKey(int keyBitsLength) {
        try {
            return generateSecretKey(keyBitsLength).getEncoded();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Generates new random key with specific length of bits
     * for AES cipher and returns it like a plain text.
     *
     * @return new random key or null if some error occurred
     */
    private static String generatePlainKey(int keyBitsLength) {
        try {
            return generateSecretKey(keyBitsLength).getEncoded().toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

}
