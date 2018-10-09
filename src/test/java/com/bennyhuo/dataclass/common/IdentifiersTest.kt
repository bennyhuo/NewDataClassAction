package com.bennyhuo.dataclass.common

import org.junit.Assert
import org.junit.Test

class IdentifiersTest{
    @Test
    fun testIdentifier(){
        Assert.assertTrue(Identifiers.isValidIdentifier("hello2"))
        Assert.assertFalse(Identifiers.isValidIdentifier("2hello2"))
        Assert.assertTrue(Identifiers.isValidIdentifier("\$hello2"))
        Assert.assertTrue(Identifiers.isValidIdentifier("\$hel\$lo2"))
        Assert.assertTrue(Identifiers.isValidIdentifier("__"))
        Assert.assertTrue(Identifiers.isValidIdentifier("_abc"))
        Assert.assertTrue(Identifiers.isValidIdentifier("_231"))
        Assert.assertFalse(Identifiers.isValidIdentifier("!_231"))
    }
}