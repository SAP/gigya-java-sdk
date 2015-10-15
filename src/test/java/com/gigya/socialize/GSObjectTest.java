package com.gigya.socialize;

import junit.framework.TestCase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.util.Arrays;


@RunWith(JUnit4.class)
public class GSObjectTest extends TestCase {

    final String KEY_NULL = "Null key";
    final String VALUE_NULL = null;
    final String KEY_STR = "Str key";
    final String VALUE_STR = "Some String";
    final String KEY_INT = "Int key";
    final int VALUE_INT = 222;
    final String KEY_INTEGER = "Integer key";
    final Integer VALUE_INTEGER = 333;
    final String KEY_BOOLEAN = "Boolean key";
    final boolean VALUE_BOOLEAN = true;
    final String KEY_BOOLEAN_OBJ = "Boolean obj key";
    final Boolean VALUE_BOOLEAN_OBJ = true;
    final String KEY_LONG = "Long key";
    final long VALUE_LONG = Long.MAX_VALUE;
    final String KEY_LONG_OBJ = "Long obj key";
    final Long VALUE_LONG_OBJ = Long.MAX_VALUE;
    final String KEY_DOUBLE = "Double key";
    final double VALUE_DOUBLE = 2.22;
    final String KEY_DOUBLE_OBJ = "Double obj key";
    final Double VALUE_DOUBLE_OBJ = 2.22;
    final String KEY_OBJ = "Object key";
    Object VALUE_OBJ = null;
    final String KEY_GIGYA_OBJ = "Gigya object key";
    GSObject VALUE_GIGYA_OBJ = null;
    final String KEY_GIGYA_ARRAY = "Gigya array key";
    GSArray VALUE_GIGYA_ARRAY = null;
    GSObject gsObject;


    @Before
    public void setUp() throws Exception {
        super.setUp();

        gsObject = new GSObject();
        VALUE_OBJ = new Object();
        VALUE_GIGYA_OBJ = new GSObject("{'a':'b', 'c':2.2, 'd':['e', {'f':null, 'g':[3, 'h']}], 'i':9223372036854775807, 'j':{'k':1}}");
        VALUE_GIGYA_ARRAY = new GSArray("['a', 2, {'b':true, 'c':['d', ['e', 9223372036854775807]]}, null]");
    }

    @Test
    public void testConstructWithJsonStringCreatesObjectWithSameData() throws Exception {
        String expectedJSONString = String.format("{'%s':'%s', '%s':%d, '%s':'%s', '%s':'%s'}", KEY_STR, VALUE_STR, KEY_INT, VALUE_INT, KEY_GIGYA_OBJ, VALUE_GIGYA_OBJ.toJsonString(), KEY_GIGYA_ARRAY, VALUE_GIGYA_ARRAY.toJsonArray());
        JSONObject expectedJSONObject = new JSONObject(expectedJSONString);
        gsObject = new GSObject(expectedJSONString);
        JSONAssert.assertEquals(gsObject.toJsonObject(), expectedJSONObject, JSONCompareMode.STRICT);
    }

    @Test
    public void testConstructWithJsonDataCreatesObjectWithSameData() throws Exception {
        String expectedJSONString = String.format("{'%s':'%s', '%s':%d, '%s':'%s', '%s':'%s'}", KEY_STR, VALUE_STR, KEY_INT, VALUE_INT, KEY_GIGYA_OBJ, VALUE_GIGYA_OBJ.toJsonString(), KEY_GIGYA_ARRAY, VALUE_GIGYA_ARRAY.toJsonArray());
        JSONObject expectedJSONObject = new JSONObject(expectedJSONString);
        gsObject = new GSObject(expectedJSONObject);
        JSONAssert.assertEquals(gsObject.toJsonObject(), expectedJSONObject, JSONCompareMode.STRICT);
    }

    @Test(expected = Exception.class)
    public void testConstructObjectWithNonJsonStringThrowsException() throws Exception {
        new GSObject("non-json-string");
    }

    @Test(expected = Exception.class)
    public void testConstructObjectWithNullJsonObjectThrowsException() throws Exception {
        new GSObject((JSONObject) null);
    }

    @Test
    public void testSavedValueEqualsToRetrievedValue() throws GSKeyNotFoundException {
        gsObject.put(KEY_STR, VALUE_STR);
        assertEquals(gsObject.get(KEY_STR), VALUE_STR);
    }

    @Test
    public void testGetKeysReturnsExpectedKeys() throws GSKeyNotFoundException {
        gsObject.put(KEY_STR, VALUE_STR);
        gsObject.put(KEY_INT, VALUE_INT);
        String[] keys = gsObject.getKeys();
        String[] expectedKeys = new String[]{KEY_STR, KEY_INT};
        Arrays.sort(keys);
        Arrays.sort(expectedKeys);
        Arrays.equals(keys, expectedKeys);
    }

    @Test
    public void testIsObjectContainsKeyReturnsExpectedResult() {
        gsObject.put(KEY_STR, VALUE_STR);
        assertFalse(gsObject.containsKey(KEY_INT));
        assertTrue(gsObject.containsKey(KEY_STR));
    }

    @Test(expected = GSKeyNotFoundException.class)
    public void testRemovingExistObjectRemovesTheObject() throws GSKeyNotFoundException {
        gsObject.put(KEY_STR, VALUE_STR);
        gsObject.remove(KEY_STR);
        gsObject.get(KEY_STR);
    }

    @Test
    public void testRemovingNonExistObjectNotThrowsException() {
        gsObject.remove(KEY_STR);
    }

    @Test
    public void testObjectToStringEqualsToExpectedString() throws JSONException {
        gsObject.put(KEY_INT, VALUE_INT);
        gsObject.put(KEY_STR, VALUE_STR);
        String expectedJSONString = String.format("{'%s':'%s', '%s':%d}", KEY_STR, VALUE_STR, KEY_INT, VALUE_INT);
        assertEquals(gsObject.toJsonString(), gsObject.toString());
        JSONAssert.assertEquals(new JSONObject(gsObject.toJsonString()), new JSONObject(expectedJSONString), JSONCompareMode.STRICT);
    }

    @Test
    public void testGetValuesWithDifferentTypesReturnsExpectedValues() throws Exception {
        gsObject.put(KEY_STR, VALUE_STR);
        gsObject.put(KEY_BOOLEAN, VALUE_BOOLEAN);
        gsObject.put(KEY_BOOLEAN_OBJ, VALUE_BOOLEAN_OBJ);
        gsObject.put(KEY_INT, VALUE_INT);
        gsObject.put(KEY_INTEGER, VALUE_INTEGER);
        gsObject.put(KEY_LONG, VALUE_LONG);
        gsObject.put(KEY_LONG_OBJ, VALUE_LONG_OBJ);
        gsObject.put(KEY_DOUBLE, VALUE_DOUBLE);
        gsObject.put(KEY_DOUBLE_OBJ, VALUE_DOUBLE_OBJ);
        gsObject.put(KEY_NULL, VALUE_NULL);
        gsObject.put(KEY_OBJ, VALUE_OBJ);
        gsObject.put(KEY_GIGYA_OBJ, VALUE_GIGYA_OBJ);
        gsObject.put(KEY_GIGYA_ARRAY, VALUE_GIGYA_ARRAY);

        assertEquals(gsObject.getString(KEY_STR), VALUE_STR);
        assertEquals(gsObject.getString(KEY_STR, ""), VALUE_STR);
        assertEquals(gsObject.getBool(KEY_BOOLEAN), VALUE_BOOLEAN);
        assertEquals(gsObject.getBool(KEY_BOOLEAN, false), VALUE_BOOLEAN);
        assertEquals(gsObject.getBool(KEY_BOOLEAN_OBJ), VALUE_BOOLEAN_OBJ.booleanValue());
        assertEquals(gsObject.getBool(KEY_BOOLEAN_OBJ, false), VALUE_BOOLEAN_OBJ.booleanValue());
        assertEquals(gsObject.getInt(KEY_INT), VALUE_INT);
        assertEquals(gsObject.getInt(KEY_INT, 123), VALUE_INT);
        assertEquals(gsObject.getInt(KEY_INTEGER), VALUE_INTEGER.intValue());
        assertEquals(gsObject.getInt(KEY_INTEGER, 123), VALUE_INTEGER.intValue());
        assertEquals(gsObject.getLong(KEY_LONG), VALUE_LONG);
        assertEquals(gsObject.getLong(KEY_LONG, 123), VALUE_LONG);
        assertEquals(gsObject.getLong(KEY_LONG_OBJ), VALUE_LONG_OBJ.longValue());
        assertEquals(gsObject.getLong(KEY_LONG_OBJ, 123), VALUE_LONG_OBJ.longValue());
        assertEquals(gsObject.getDouble(KEY_DOUBLE), VALUE_DOUBLE);
        assertEquals(gsObject.getDouble(KEY_DOUBLE, 1.23), VALUE_DOUBLE);
        assertEquals(gsObject.getDouble(KEY_DOUBLE_OBJ), VALUE_DOUBLE_OBJ);
        assertEquals(gsObject.getDouble(KEY_DOUBLE_OBJ, 1.23), VALUE_DOUBLE_OBJ);
        assertEquals(gsObject.get(KEY_NULL), VALUE_NULL);
        assertEquals(gsObject.get(KEY_OBJ), VALUE_OBJ);
        assertEquals(gsObject.get(KEY_OBJ, null), VALUE_OBJ);
        assertEquals(gsObject.getObject(KEY_GIGYA_OBJ), VALUE_GIGYA_OBJ);
        assertEquals(gsObject.getObject(KEY_GIGYA_OBJ, null), VALUE_GIGYA_OBJ);
        assertEquals(gsObject.getArray(KEY_GIGYA_ARRAY), VALUE_GIGYA_ARRAY);
        assertEquals(gsObject.getArray(KEY_GIGYA_ARRAY, null), VALUE_GIGYA_ARRAY);
    }

    @Test
    public void testGetDefaultValueOnNonExistKeyReturnsDefaultValue() throws Exception {
        JSONObject defaultJSONObject = new JSONObject();
        GSObject defaultGSObject = new GSObject();
        GSArray defaultGSArray = new GSArray();

        assertEquals(gsObject.getString(KEY_STR, "Some default string"), "Some default string");
        assertEquals(gsObject.getBool(KEY_BOOLEAN, true), true);
        assertEquals(gsObject.getInt(KEY_INT, 123), 123);
        assertEquals(gsObject.getLong(KEY_LONG, 123), 123);
        assertEquals(gsObject.getDouble(KEY_DOUBLE, 1.23), 1.23);
        assertEquals(gsObject.get(KEY_OBJ, defaultJSONObject), defaultJSONObject);
        assertEquals(gsObject.getObject(KEY_GIGYA_OBJ, defaultGSObject), defaultGSObject);
        assertEquals(gsObject.getArray(KEY_GIGYA_ARRAY, defaultGSArray), defaultGSArray);
    }

    @Test(expected = NullPointerException.class)
    public void testGetNullValueAsBooleanThrowsException() throws Exception {
        gsObject.getMap().put(KEY_BOOLEAN, null);
        gsObject.getBool(KEY_BOOLEAN);
    }

    @Test(expected = NullPointerException.class)
    public void testGetNullValueAsIntThrowsException() throws Exception {
        gsObject.getMap().put(KEY_INT, null);
        gsObject.getInt(KEY_INT);
    }

    @Test(expected = NullPointerException.class)
    public void testGetNullValueAsLongThrowsException() throws Exception {
        gsObject.getMap().put(KEY_LONG, null);
        gsObject.getLong(KEY_LONG);
    }

    @Test(expected = NullPointerException.class)
    public void testGetNullValueAsDoubleThrowsException() throws Exception {
        gsObject.getMap().put(KEY_DOUBLE, null);
        gsObject.getDouble(KEY_DOUBLE);
    }

    @Test
    public void testGetNullValueReturnsNull() throws GSKeyNotFoundException {
        gsObject.getMap().put(KEY_STR, null);
        gsObject.getMap().put(KEY_OBJ, null);
        gsObject.getMap().put(KEY_GIGYA_OBJ, null);
        gsObject.getMap().put(KEY_GIGYA_ARRAY, null);

        assertNull(gsObject.getString(KEY_STR));
        assertNull(gsObject.get(KEY_OBJ));
        assertNull(gsObject.getObject(KEY_GIGYA_OBJ));
        assertNull(gsObject.getArray(KEY_GIGYA_ARRAY));
    }

    @Test
    public void testGetBooleanFromStringReturnsExpectedValue() throws Exception {
        gsObject.put(KEY_STR, "true");
        assertEquals(gsObject.getBool(KEY_STR), true);
        gsObject.put(KEY_STR, "false");
        assertEquals(gsObject.getBool(KEY_STR), false);
        gsObject.put(KEY_STR, "1");
        assertEquals(gsObject.getBool(KEY_STR), true);
        gsObject.put(KEY_STR, "0");
        assertEquals(gsObject.getBool(KEY_STR), false);
    }

    @Test
    public void testGetIntFromStringReturnsExpectedValue() throws Exception {
        gsObject.put(KEY_STR, "0");
        assertEquals(gsObject.getInt(KEY_STR), 0);
        gsObject.put(KEY_STR, "473");
        assertEquals(gsObject.getInt(KEY_STR), 473);
        gsObject.put(KEY_STR, "+42");
        assertEquals(gsObject.getInt(KEY_STR), 42);
        gsObject.put(KEY_STR, "-2147483648");
        assertEquals(gsObject.getInt(KEY_STR), -2147483648);
        gsObject.put(KEY_STR, "2147483647");
        assertEquals(gsObject.getInt(KEY_STR), 2147483647);
    }

    @Test(expected = NumberFormatException.class)
    public void testGetIntFromNonDigitStringThrowsException() throws Exception {
        gsObject.put(KEY_STR, VALUE_STR);
        gsObject.getInt(KEY_STR);
    }

    @Test
    public void testGetLongFromStringReturnsExpectedValue() throws Exception {
        gsObject.put(KEY_STR, "0");
        assertEquals(gsObject.getLong(KEY_STR), 0L);
        gsObject.put(KEY_STR, "473");
        assertEquals(gsObject.getLong(KEY_STR), 473L);
        gsObject.put(KEY_STR, "+42");
        assertEquals(gsObject.getLong(KEY_STR), 42L);
        gsObject.put(KEY_STR, "-9223372036854775808");
        assertEquals(gsObject.getLong(KEY_STR), -9223372036854775808L);
        gsObject.put(KEY_STR, "9223372036854775807");
        assertEquals(gsObject.getLong(KEY_STR), 9223372036854775807L);
    }

    @Test(expected = NumberFormatException.class)
    public void testGetLongFromNonDigitStringThrowsException() throws Exception {
        gsObject.put(KEY_STR, VALUE_STR);
        gsObject.getLong(KEY_STR);
    }

    @Test
    public void testGetDoubleFromStringReturnsExpectedValue() throws Exception {
        gsObject.put(KEY_STR, "0");
        assertEquals(gsObject.getDouble(KEY_STR), 0D);
        gsObject.put(KEY_STR, "473");
        assertEquals(gsObject.getDouble(KEY_STR), 473D);
        gsObject.put(KEY_STR, "+42");
        assertEquals(gsObject.getDouble(KEY_STR), 42D);
        gsObject.put(KEY_STR, "4.9E-324");
        assertEquals(gsObject.getDouble(KEY_STR), 4.9E-324D);
        gsObject.put(KEY_STR, "1.7976931348623157E308");
        assertEquals(gsObject.getDouble(KEY_STR), 1.7976931348623157E308D);
    }

    @Test(expected = NumberFormatException.class)
    public void testGetDoubleFromNonDigitStringThrowsException() throws Exception {
        gsObject.put(KEY_STR, VALUE_STR);
        gsObject.getDouble(KEY_STR);
    }

    @Test
    public void testObjectClearRemovesAllKeys() {
        assertTrue(VALUE_GIGYA_OBJ.getKeys().length > 0);
        VALUE_GIGYA_OBJ.clear();
        assertTrue(VALUE_GIGYA_OBJ.getKeys().length == 0);
    }

    @Test
    public void testDeepClonedObjectEqualsToOriginal() throws Exception {
        JSONAssert.assertEquals(VALUE_GIGYA_OBJ.toJsonObject(), VALUE_GIGYA_OBJ.clone().toJsonObject(), JSONCompareMode.STRICT);
    }

    @Test
    public void testUrlIsParsedIntoObjectAsExpected() throws Exception {
        gsObject.parseURL("http://example.com?a=2&b=c&d=true&e=9223372036854775807&f=2.2&g={a:2,b:c}&h=[a,b,3]");
        assertEquals(gsObject.getInt("a"), 2);
        assertEquals(gsObject.getString("b"), "c");
        assertEquals(gsObject.getBool("d"), true);
        assertEquals(gsObject.getLong("e"), 9223372036854775807L);
        assertEquals(gsObject.getDouble("f"), 2.2);
        JSONAssert.assertEquals(new JSONObject(gsObject.getString("g")), new JSONObject("{a:2,b:c})"), JSONCompareMode.STRICT);
        JSONAssert.assertEquals(new JSONArray(gsObject.getString("h")), new JSONArray("[a,b,3]"), JSONCompareMode.STRICT);
    }

    @Test
    public void testParseMalformedUrlNotThrowsException() {
        gsObject.parseURL("invalid_protocol://domain.com?a=b");
        assertEquals(gsObject.getKeys().length, 0);
    }
}