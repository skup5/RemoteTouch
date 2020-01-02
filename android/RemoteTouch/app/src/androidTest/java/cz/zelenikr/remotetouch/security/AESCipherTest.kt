package cz.zelenikr.remotetouch.security

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.logging.Logger

class AESCipherTest {

    private val logger = Logger.getLogger(javaClass.simpleName)
    private lateinit var cipher: SymmetricCipher<String>

    @Before
    fun beforeTest() {
        val key = "hO7nA1nNK4"
        cipher = AESCipher(key)
    }

    @Test
    fun cipher() {
        val plainAESKey = AESKeyGenerator().generate()
        logger.severe("key: $plainAESKey")
        val cipher = AESCipher(plainAESKey)
        val plainText = "some plain text\nsecond plain text"
        val encrypted = cipher.encrypt(plainText)
        logger.severe("$plainText \n->")
        logger.severe(encrypted)
//        logger.debug(encrypted ?: "?")
        val decrypted = encrypted?.let { cipher.decrypt(it) } ?: "?"
        logger.severe(decrypted)
        assertEquals(plainText, decrypted)
    }

    @Test
    fun encrypt() {
        val plainText = """{"type":"sms", "content": {"number": 789456123, "text": "some plain text,${'\n'}some another text"}}"""
        val encrypted = cipher.encrypt(plainText)
        logger.severe("$plainText \n->")
        logger.severe(encrypted)
    }

    @Test
    fun decrypt() {
        val plainText = """{"type":"sms", "content": {"number": 789456123, "text": "some plain text,${'\n'}some another text"}}"""
        val encrypted = """IYQSZxvZx6CdSEoqvs+KEJrJYTxKJdv92LfAlt0ANSc3PGL+L+epD0JNQaRLReAUr2c3fss1O+CN
CitLZ0Rj2ro+p78IsCSsu9OqxDRL759H85CH7iTD0nkOEr+l2Zkn"""
        val decrypted = cipher.decrypt(encrypted)
        assertEquals(plainText, decrypted)
    }
}