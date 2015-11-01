package com.gigya.socialize;

import junit.framework.TestCase;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;


@RunWith(JUnit4.class)
public class GSArrayTest extends TestCase {

    final String VALUE_STR_NULL = null;
    final Integer VALUE_INTEGER_NULL = null;
    final Long VALUE_LONG_NULL = null;
    final Double VALUE_DOUBLE_NULL = null;
    final Boolean VALUE_BOOLEAN_NULL = null;
    final Object VALUE_OBJ_NULL = null;
    final GSObject VALUE_GIGYA_OBJ_NULL = null;
    final GSArray VALUE_GIGYA_ARRAY_NULL = null;
    final String VALUE_STR = "Some String";
    final int VALUE_INT = 222;
    final Integer VALUE_INTEGER = 333;
    final boolean VALUE_BOOLEAN = true;
    final Boolean VALUE_BOOLEAN_OBJ = true;
    final long VALUE_LONG = Long.MAX_VALUE;
    final Long VALUE_LONG_OBJ = Long.MAX_VALUE;
    final double VALUE_DOUBLE = 2.22;
    final Double VALUE_DOUBLE_OBJ = 2.22;
    GSObject VALUE_GIGYA_OBJ = null;
    GSArray VALUE_GIGYA_ARRAY = null;
    GSArray gsArray;


    @Before
    public void setUp() throws Exception {
        super.setUp();

        gsArray = new GSArray();
        VALUE_GIGYA_OBJ = new GSObject("{'a':'b', 'c':2.2, 'd':['e', {'f':null, 'g':[3, 'h']}], 'i':9223372036854775807, 'j':{'k':1}}");
        VALUE_GIGYA_ARRAY = new GSArray("['a', 2, 3.3, {'b':true, 'c':['d', ['e', 9223372036854775807]]}, null]");
    }

    @Test
    public void testConstructWithJsonStringCreatesArrayWithSameData() throws Exception {
        String expectedJSONString = String.format("[null, '%s', %d, null, '%s', '%s', null]", VALUE_STR, VALUE_INT, VALUE_GIGYA_OBJ.toJsonString(), VALUE_GIGYA_ARRAY.toJsonArray());
        JSONArray expectedJSONArray = new JSONArray(expectedJSONString);
        gsArray = new GSArray(expectedJSONString);
        JSONAssert.assertEquals(gsArray.toJsonArray(), expectedJSONArray, JSONCompareMode.STRICT);
    }

    @Test
    public void testConstructWithJsonDataCreatesArrayWithSameData() throws Exception {
        String expectedJSONString = String.format("[null, '%s', %d, null, '%s', '%s', null]", VALUE_STR, VALUE_INT, VALUE_GIGYA_OBJ.toJsonString(), VALUE_GIGYA_ARRAY.toJsonArray());
        JSONArray expectedJSONArray = new JSONArray(expectedJSONString);
        gsArray = new GSArray(expectedJSONArray);
        JSONAssert.assertEquals(gsArray.toJsonArray(), expectedJSONArray, JSONCompareMode.STRICT);
    }

    @Test(expected = Exception.class)
    public void testConstructArrayWithNonJsonStringThrowsException() throws Exception {
        new GSArray("not a JSON string");
    }

    @Test(expected = Exception.class)
    public void testConstructArrayWithNullJsonArrayThrowsException() throws Exception {
        new GSArray((JSONArray) null);
    }

    @Test
    public void testAddedValueEqualsToRetrievedValue() throws GSKeyNotFoundException {
        gsArray.add(VALUE_STR);
        assertEquals(gsArray.getString(0), VALUE_STR);
    }

    @Test
    public void testArrayToStringEqualsToExpectedString() throws JSONException {
        gsArray.add(VALUE_STR_NULL);
        gsArray.add(VALUE_INT);
        gsArray.add(VALUE_STR);
        gsArray.add(VALUE_BOOLEAN);
        gsArray.add(VALUE_INTEGER_NULL);
        gsArray.add(VALUE_LONG_NULL);
        gsArray.add(VALUE_DOUBLE_NULL);
        gsArray.add(VALUE_BOOLEAN_NULL);
        gsArray.add(VALUE_OBJ_NULL);
        gsArray.add(VALUE_GIGYA_OBJ_NULL);
        gsArray.add(VALUE_GIGYA_ARRAY_NULL);

        String expectedJSONString = String.format("[%s, %d, '%s', %s, %s, %s, %s, %s, %s, %s, %s]",
                VALUE_STR_NULL,
                VALUE_INT,
                VALUE_STR,
                VALUE_BOOLEAN,
                VALUE_INTEGER_NULL,
                VALUE_LONG_NULL,
                VALUE_DOUBLE_NULL,
                VALUE_BOOLEAN_NULL,
                VALUE_OBJ_NULL,
                VALUE_GIGYA_OBJ_NULL,
                VALUE_GIGYA_ARRAY_NULL
        );

        assertEquals(gsArray.toJsonString(), gsArray.toString());
        JSONAssert.assertEquals(new JSONArray(gsArray.toJsonString()), new JSONArray(expectedJSONString), JSONCompareMode.STRICT);
    }

    @Test
    public void testGetValuesWithDifferentTypesReturnsExpectedValues() throws Exception {
        gsArray.add(VALUE_STR);
        gsArray.add(VALUE_BOOLEAN);
        gsArray.add(VALUE_BOOLEAN_OBJ);
        gsArray.add(VALUE_INT);
        gsArray.add(VALUE_INTEGER);
        gsArray.add(VALUE_LONG);
        gsArray.add(VALUE_LONG_OBJ);
        gsArray.add(VALUE_DOUBLE);
        gsArray.add(VALUE_DOUBLE_OBJ);
        gsArray.add(VALUE_GIGYA_OBJ);
        gsArray.add(VALUE_GIGYA_ARRAY);
        gsArray.add(VALUE_STR_NULL);
        gsArray.add(VALUE_INTEGER_NULL);
        gsArray.add(VALUE_LONG_NULL);
        gsArray.add(VALUE_DOUBLE_NULL);
        gsArray.add(VALUE_BOOLEAN_NULL);
        gsArray.add(VALUE_OBJ_NULL);
        gsArray.add(VALUE_GIGYA_OBJ_NULL);
        gsArray.add(VALUE_GIGYA_ARRAY_NULL);

        assertEquals(gsArray.getString(0), VALUE_STR);
        assertEquals(gsArray.getBool(1), VALUE_BOOLEAN);
        assertEquals(gsArray.getBool(2), VALUE_BOOLEAN_OBJ.booleanValue());
        assertEquals(gsArray.getInt(3), VALUE_INT);
        assertEquals(gsArray.getInt(4), VALUE_INTEGER.intValue());
        assertEquals(gsArray.getLong(5), VALUE_LONG);
        assertEquals(gsArray.getLong(6), VALUE_LONG_OBJ.longValue());
        assertEquals(gsArray.getDouble(7), VALUE_DOUBLE);
        assertEquals(gsArray.getDouble(8), VALUE_DOUBLE_OBJ);
        assertEquals(gsArray.getObject(9), VALUE_GIGYA_OBJ);
        assertEquals(gsArray.getArray(10), VALUE_GIGYA_ARRAY);
        assertEquals(gsArray.getString(11), VALUE_STR_NULL);
        assertEquals(gsArray.get(12), VALUE_INTEGER_NULL);
        assertEquals(gsArray.get(13), VALUE_LONG_NULL);
        assertEquals(gsArray.get(14), VALUE_DOUBLE_NULL);
        assertEquals(gsArray.get(15), VALUE_BOOLEAN_NULL);
        assertEquals(gsArray.get(16), VALUE_OBJ_NULL);
        assertEquals(gsArray.get(17), VALUE_GIGYA_OBJ_NULL);
        assertEquals(gsArray.get(18), VALUE_GIGYA_ARRAY_NULL);
    }

    @Test
    public void testGetArrayLengthReturnsExpectedLength() {
        gsArray.add(VALUE_STR);
        gsArray.add(VALUE_BOOLEAN);
        assertEquals(gsArray.length(), 2);
    }

    @Test(expected = NullPointerException.class)
    public void testGetNullValueAsBooleanThrowsException() throws Exception {
        gsArray.getUnderlingArray().add(null);
        gsArray.getBool(0);
    }

    @Test(expected = NullPointerException.class)
    public void testGetNullValueAsIntThrowsException() throws Exception {
        gsArray.getUnderlingArray().add(null);
        gsArray.getInt(0);
    }

    @Test(expected = NullPointerException.class)
    public void testGetNullValueAsLongThrowsException() throws Exception {
        gsArray.getUnderlingArray().add(null);
        gsArray.getLong(0);
    }

    @Test(expected = NullPointerException.class)
    public void testGetNullValueAsDoubleThrowsException() throws Exception {
        gsArray.getUnderlingArray().add(null);
        gsArray.getDouble(0);
    }

    @Test
    public void testGetNullValueReturnsNull() throws GSKeyNotFoundException {
        gsArray.getUnderlingArray().add(null);
        gsArray.getUnderlingArray().add(null);
        gsArray.getUnderlingArray().add(null);

        assertNull(gsArray.getString(0));
        assertNull(gsArray.getObject(1));
        assertNull(gsArray.getArray(2));
    }

    @Test
    public void testGetBooleanFromStringReturnsExpectedValue() throws Exception {
        gsArray.add("true");
        assertEquals(gsArray.getBool(0), true);
        gsArray.add("false");
        assertEquals(gsArray.getBool(1), false);
        gsArray.add("1");
        assertEquals(gsArray.getBool(2), true);
        gsArray.add("0");
        assertEquals(gsArray.getBool(3), false);
    }

    @Test
    public void testGetIntFromStringReturnsExpectedValue() throws Exception {
        gsArray.add("0");
        assertEquals(gsArray.getInt(0), 0);
        gsArray.add("473");
        assertEquals(gsArray.getInt(1), 473);
        gsArray.add("+42");
        assertEquals(gsArray.getInt(2), 42);
        gsArray.add("-2147483648");
        assertEquals(gsArray.getInt(3), -2147483648);
        gsArray.add("2147483647");
        assertEquals(gsArray.getInt(4), 2147483647);
    }

    @Test(expected = NumberFormatException.class)
    public void testGetIntFromNonDigitStringThrowsException() throws Exception {
        gsArray.add(VALUE_STR);
        gsArray.getInt(0);
    }

    @Test
    public void testGetLongFromStringReturnsExpectedValue() throws Exception {
        gsArray.add("0");
        assertEquals(gsArray.getLong(0), 0L);
        gsArray.add("473");
        assertEquals(gsArray.getLong(1), 473L);
        gsArray.add("+42");
        assertEquals(gsArray.getLong(2), 42L);
        gsArray.add("-9223372036854775808");
        assertEquals(gsArray.getLong(3), -9223372036854775808L);
        gsArray.add("9223372036854775807");
        assertEquals(gsArray.getLong(4), 9223372036854775807L);
    }

    @Test(expected = NumberFormatException.class)
    public void testGetLongFromNonDigitStringThrowsException() throws Exception {
        gsArray.add(VALUE_STR);
        gsArray.getLong(0);
    }

    @Test
    public void testGetDoubleFromStringReturnsExpectedValue() throws Exception {
        gsArray.add("0");
        assertEquals(gsArray.getDouble(0), 0D);
        gsArray.add("473");
        assertEquals(gsArray.getDouble(1), 473D);
        gsArray.add("+42");
        assertEquals(gsArray.getDouble(2), 42D);
        gsArray.add("4.9E-324");
        assertEquals(gsArray.getDouble(3), 4.9E-324D);
        gsArray.add("1.7976931348623157E308");
        assertEquals(gsArray.getDouble(4), 1.7976931348623157E308D);
    }

    @Test(expected = NumberFormatException.class)
    public void testGetDoubleFromNonDigitStringThrowsException() throws Exception {
        gsArray.add(VALUE_STR);
        gsArray.getDouble(0);
    }

    @Test
    public void testGetIteratorReturnsNotNull() {
        assertNotNull(gsArray.iterator());
    }
}