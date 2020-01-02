package cz.zelenikr.remotetouch.security

import android.support.test.runner.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SHAHashTest {

    @Test
    fun hashOldGeneratedKey(){
        assertEquals("R7eG00h/U9wVRkiITVOpZWQJT9E1YZmTAIXBuRbNfz4=", SHAHash().hash("XA1[B@6799e8e"))
    }

    @Test
    fun hashNewGeneratedKey(){
        assertEquals("W3pj4LHfgr/269ifY/FgqENwVJKV4zfYuHThJVcxugo=", SHAHash().hash("XA1hO7nA1nNK4"))
    }
}