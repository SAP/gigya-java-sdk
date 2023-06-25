package com.gigya.socialize;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;


@RunWith(JUnit4.class)
public class Base64Test extends TestCase {

    String string = "Some String";
    String base64String = "U29tZSBTdHJpbmc=";
    byte[] bytes;


    @Before
    public void setUp() throws Exception {
        super.setUp();

        bytes = string.getBytes("UTF-8");
    }

    @Test
    public void testEncodeBytesToBase64ReturnsExpectedString() {
        assertEquals(Base64.encodeToString(bytes, false), base64String);
    }

    @Test
    public void testDecodeStringFromBase64ReturnsExpectedString() {
        assertTrue(Arrays.equals(Base64.decode(base64String), bytes));
    }
}