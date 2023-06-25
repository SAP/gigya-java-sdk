package com.gigya.socialize;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;


@RunWith(JUnit4.class)
public class GSLoggerTest extends TestCase {

    final int MAX_STRING_LENGTH = 10000;
    GSLogger logger;


    @Before
    public void setUp() throws Exception {
        logger = new GSLogger();
    }

    @Test
    public void testGetLogAfterWritingKeyValueReturnsExpectedString() {
        GSObject gigyaObj = new GSObject();
        gigyaObj.put("Some key", "Some value");
        logger.write("Some key", gigyaObj);
        assertEquals(logger.toString(), "Some key: " + gigyaObj.toString() + "\n");
    }

    @Test
    public void testGetLogAfterWritingObjectReturnsExpectedString() {
        GSObject gigyaObj = new GSObject();
        gigyaObj.put("Some key", "Some value");
        logger.write(gigyaObj);
        assertEquals(logger.toString(), gigyaObj.toString() + "\n");
    }

    @Test
    public void testGetLogAfterWritingExceptionReturnsExpectedString() {
        Exception ex = new Exception("Some Exception");
        ex.fillInStackTrace();
        logger.write(ex);
        assertEquals(logger.toString(), getStackTrace(ex) + "\n");
    }

    @Test
    public void testGetLogAfterWritingStringFormatReturnsExpectedString() {
        logger.writeFormat("String %s %s", "with", "format");
        assertEquals(logger.toString(), "String with format\n");
    }

    @Test
    public void testWriteTooLongStringReturnsTruncatedString() {
        String tooLongString = generateTooLongString();
        logger.write(tooLongString);
        assertEquals(logger.toString(), tooLongString.substring(0, MAX_STRING_LENGTH) + ".. (value too long)\n");
    }

    private String getStackTrace(Throwable ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }

    private String generateTooLongString() {
        int length = MAX_STRING_LENGTH + 1;
        char[] array = new char[length];
        Arrays.fill(array, 'a');
        return new String(array);
    }
}