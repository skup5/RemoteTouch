package cz.zelenikr.remotetouch.security

import java.security.SecureRandom

/**
 * Simple generator of AES plain text keys.
 *
 * @author Roman Zelenik
 */
class AESKeyGenerator
/**
 * Initialize new key generator with specific key length.
 *
 * @param keyBytesLength required length (in bytes)
 */
(private val keyBytesLength: Int = DEFAULT_KEY_BYTES_LENGTH) : SymmetricKeyGenerator<String> {

    override fun generate(): String {
        return generatePlainKey(keyBytesLength)
    }

    companion object {

        private const val DEFAULT_KEY_BYTES_LENGTH = 10

        /**
         * Generates new random key with specific length of bytes
         * for AES cipher and returns it like a plain text.
         *
         * @return new random key or null if some error occurred
         */
        private fun generatePlainKey(keyBytesLength: Int): String {
            return generatePassword(keyBytesLength)
        }

        private fun generatePassword(length: Int,
                                     upperCase: Boolean = true, lowerCase: Boolean = true,
                                     numeric: Boolean = true,
                                     specialChar: Boolean = true): String {

            val availableChars = mutableListOf<Char>()
            if (upperCase) availableChars += ('A'..'Z')
            if (lowerCase) availableChars += ('a'..'z')
            if (numeric) availableChars += ('0'..'9')
            if (specialChar) availableChars += arrayOf('{', '}', '[', ']', '@', '#', '?', '!')

            val randomGenerator = SecureRandom()
            val password = CharArray(length)
            for (i in 0 until length) {
                password[i] = availableChars[randomGenerator.nextInt(availableChars.size)]
            }

            return String(password)
        }
    }

}
