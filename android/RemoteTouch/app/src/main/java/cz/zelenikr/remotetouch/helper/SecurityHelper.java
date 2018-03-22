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

    public static <T> SymmetricCipher<T> createSymmetricCipherInstance(@NonNull T symmetricKey) throws UnsupportedCipherException {
        if (symmetricKey instanceof String)
            return (SymmetricCipher<T>) new AESCipher((String) symmetricKey);
        else
            throw new UnsupportedCipherException("Unsupported type of key <" + symmetricKey.getClass().getSimpleName() + ">.");
    }

    public static SymmetricKeyGenerator<String> createSymmetricKeyGeneratorInstance() {
        return new AESKeyGenerator();
    }

    public static Hash createHashInstance() {
        return new SHAHash();
    }
}
