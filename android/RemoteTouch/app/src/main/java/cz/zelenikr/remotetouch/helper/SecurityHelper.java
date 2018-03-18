package cz.zelenikr.remotetouch.helper;

import android.support.annotation.NonNull;


import cz.zelenikr.remotetouch.security.AESCipher;
import cz.zelenikr.remotetouch.security.AESKeyGenerator;
import cz.zelenikr.remotetouch.security.Hash;
import cz.zelenikr.remotetouch.security.SHAHash;
import cz.zelenikr.remotetouch.security.SymmetricCipher;
import cz.zelenikr.remotetouch.security.SymmetricKeyGenerator;
import cz.zelenikr.remotetouch.security.exception.UnsupportedCipherException;

/**
 * @author Roman Zelenik
 */
public final class SecurityHelper {

    public static SymmetricCipher createSymmetricCipherInstance(@NonNull String symmetricKey) throws UnsupportedCipherException {
        return new AESCipher(symmetricKey);
    }

    public static SymmetricKeyGenerator<String> createSymmetricKeyGeneratorInstance() {
        return new AESKeyGenerator();
    }

    public static Hash createHashInstance() {
        return new SHAHash();
    }
}
