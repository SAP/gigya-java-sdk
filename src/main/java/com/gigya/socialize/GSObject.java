/*
 * Copyright (C) 2011 Gigya, Inc.
 * Version java_3.0
 */

package main.java.com.gigya.socialize;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.TreeMap;

import main.java.com.gigya.json.JSONArray;
import main.java.com.gigya.json.JSONException;
import main.java.com.gigya.json.JSONObject;

/**
 * Used for passing parameters, for example when issuing requests or receiving response data.  <br/>
 * The object holds [key, value] pairs. The key represents a parameter name (type String) and the value may be one of the following types: <br/>
 * <ul>
 * <li> String
 * <li> boolean
 * <li> int
 * <li> long
 * <li> double
 * <li> GSObject
 * <li> GSArray
 * </ul>
 * @author Raviv Pavel
 **/
@SuppressWarnings("serial")
public class GSObject  implements Serializable
{
	// using tree map to ensure alphabetic order of keys
	// important when calculating base string for OAuth1 signatures
	private TreeMap<String, Object> map = new TreeMap<String, Object>();
	private static final String NO_KEY_EX = "GSObject does not contain a value for key ";
	/* PUBLIC INTERFACE */
	public GSObject() {}

	/**
	 * Construct a GSObject from a JSON string. Throws an Exception if unable to parse the JSON string.
	 * @param json the JSON formatted string
	 * @throws Exception if unable to parse the JSON string
	 */
	public GSObject(String json) throws Exception
	{
		JSONObject jo = new JSONObject(json);
		processJsonObject(jo, this);
	}

	/**
	 * Construct a GSObject from a JSONObject - used internally
	 * @param jo the JSON object to parse
	 * @throws Exception if unable to parse JSON
	 */
	protected GSObject(JSONObject jo) throws Exception
	{
		processJsonObject(jo, this);
	}

	/* PUTS */
	/**
	 * Associates the specified value with the specified key in this object.
	 * If the object previously contained a mapping for the key, the old value is replaced by the specified value.
	 * @param key key with which the specified value is to be associated
	 * @param value a string value to be associated with the specified key
	 */
	public void put(String key, String value)
	{
		if (key==null || value==null) return;
		map.put(key, value);
	}

	/**
	 * Associates the specified value with the specified key in this object.
	 * If the object previously contained a mapping for the key, the old value is replaced by the specified value.
	 * @param key key with which the specified value is to be associated
	 * @param value a int value to be associated with the specified key
	 */
	public void put(String key, int value)
	{
		if (key==null) return;
		map.put(key, value);
	}

	/**
	 * Associates the specified value with the specified key in this object.
	 * If the object previously contained a mapping for the key, the old value is replaced by the specified value.
	 * @param key key with which the specified value is to be associated
	 * @param value a long value to be associated with the specified key
	 */
	public void put(String key, long value)
	{
		if (key==null) return;
		map.put(key, value);
	}

	/**
	 * Associates the specified value with the specified key in this object.
	 * If the object previously contained a mapping for the key, the old value is replaced by the specified value.
	 * @param key key with which the specified value is to be associated
	 * @param value a double value to be associated with the specified key
	 */
	public void put(String key, double value)
	{
		if (key==null) return;
		map.put(key, value);
	}

	/**
	 * Associates the specified value with the specified key in this object.
	 * If the object previously contained a mapping for the key, the old value is replaced by the specified value.
	 * @param key key with which the specified value is to be associated
	 * @param value a boolean value to be associated with the specified key
	 */
	public void put(String key, boolean value)
	{
		if (key==null) return;
		map.put(key, value);
	}

	/**
	 * Associates the specified value with the specified key in this object.
	 * If the object previously contained a mapping for the key, the old value is replaced by the specified value.
	 * @param key key with which the specified value is to be associated
	 * @param value a GSObject value to be associated with the specified key
	 */
	public void put(String key, GSObject value)
	{
		if (key==null) return;
		map.put(key, value);
	}

	/**
	 * Associates the specified value with the specified key in this object.
	 * If the object previously contained a mapping for the key, the old value is replaced by the specified value.
	 * @param key key with which the specified value is to be associated
	 * @param value a GSArray value to be associated with the specified key
	 */
	public void put(String key, GSArray value)
	{
		map.put(key, value);
	}

    public void put(String key, Object value)
    {
        map.put(key, value);
    }

	/* GETS */
	/**
	 * Returns the boolean value to which the specified key is mapped, or the <em>defaultValue</em> if this object contains no mapping for the key.
	 * @param key the key whose associated value is to be returned
	 * @param defaultValue the boolean value to be returned if this object doesn't contain the specified key.
	 * @return the boolean value to which the specified key is mapped, or the <em>defaultValue</em> if this object contains no mapping for the key.
	 */
	public boolean getBool(String key, boolean defaultValue)
	{
		try {
			return getBool(key);
		} catch (Exception ex)
		{
			return defaultValue;
		}
	}

	/**
	 * Returns the boolean value to which the specified key is mapped.
	 * @param key the key whose associated value is to be returned
	 * @throws GSKeyNotFoundException if this object contains no mapping for the specified
	 * @return the boolean value to which the specified key is mapped.
	 */
	public boolean getBool(String key) throws GSKeyNotFoundException, NullPointerException, InvalidClassException
	{
		if (!map.containsKey(key))
			throw new GSKeyNotFoundException(NO_KEY_EX+key);

		Object obj = map.get(key);
		if (obj==null)
			throw new NullPointerException(NO_KEY_EX+key);

		if (obj.getClass().isAssignableFrom(Boolean.class))
		{
			return (Boolean)obj;
		} else
		{
			return obj.toString().toLowerCase().equals("true") || obj.toString().equals("1");
		}

	}

	/**
	 * Returns the int value to which the specified key is mapped, or the <em>defaultValue</em> if this object contains no mapping for the key.
	 * @param key the key whose associated value is to be returned
	 * @param defaultValue the int value to be returned if this object doesn't contain the specified key.
	 * @return the int value to which the specified key is mapped, or the <em>defaultValue</em> if this object contains no mapping for the key.
	 */
	public int getInt(String key, int defaultValue)
	{
		try {
			return getInt(key);
		} catch (Exception ex)
		{
			return defaultValue;
		}
	}

	/**
	 * Returns the int value to which the specified key is mapped.
	 * @param key the key whose associated value is to be returned
	 * @throws GSKeyNotFoundException if this object contains no mapping for the specified
	 * @return the int value to which the specified key is mapped.
	 */
	public int getInt(String key) throws GSKeyNotFoundException, NullPointerException, InvalidClassException
	{
		if (!map.containsKey(key))
			throw new GSKeyNotFoundException(NO_KEY_EX+key);

		Object obj = map.get(key);
		if (obj==null)
			throw new NullPointerException(NO_KEY_EX+key);

		if (obj.getClass().isAssignableFrom(int.class))
		{
			return (Integer)obj;
		} else
		{
			return Integer.parseInt(getString(key));
		}

	}

	/**
	 * Returns the long value to which the specified key is mapped, or the <em>defaultValue</em> if this object contains no mapping for the key.
	 * @param key the key whose associated value is to be returned
	 * @param defaultValue the long value to be returned if this object doesn't contain the specified key.
	 * @return the long value to which the specified key is mapped, or the <em>defaultValue</em> if this object contains no mapping for the key.
	 */
	public long getLong(String key, long defaultValue)
	{
		try {
			return getLong(key);
		} catch (Exception ex)
		{
			return defaultValue;
		}
	}

	/**
	 * Returns the long value to which the specified key is mapped.
	 * @param key the key whose associated value is to be returned
	 * @throws GSKeyNotFoundException if this object contains no mapping for the specified
	 * @return the long value to which the specified key is mapped.
	 */
	public long getLong(String key) throws GSKeyNotFoundException, NullPointerException, InvalidClassException
	{
		if (!map.containsKey(key))
			throw new GSKeyNotFoundException(NO_KEY_EX+key);

		Object obj = map.get(key);
		if (obj==null)
			throw new NullPointerException(NO_KEY_EX+key);

		if (obj.getClass().isAssignableFrom(long.class))
		{
			return (Long)obj;
		} else
		{
			return Long.parseLong(getString(key));
		}
	}

	/**
	 * Returns the double value to which the specified key is mapped, or the <em>defaultValue</em> if this object contains no mapping for the key.
	 * @param key the key whose associated value is to be returned
	 * @param defaultValue the long value to be returned if this object doesn't contain the specified key.
	 * @return the double value to which the specified key is mapped, or the <em>defaultValue</em> if this object contains no mapping for the key.
	 */
	public double getDouble(String key, double defaultValue)
	{
		try {
			return getLong(key);
		} catch (Exception ex)
		{
			return defaultValue;
		}
	}

	/**
	 * Returns the double value to which the specified key is mapped.
	 * @param key the key whose associated value is to be returned
	 * @throws GSKeyNotFoundException if this object contains no mapping for the specified
	 * @return the double value to which the specified key is mapped.
	 */
	public double getDouble(String key) throws GSKeyNotFoundException, NullPointerException, InvalidClassException
	{
		if (!map.containsKey(key))
			throw new GSKeyNotFoundException(NO_KEY_EX+key);

		Object obj = map.get(key);
		if (obj==null)
			throw new NullPointerException(NO_KEY_EX+key);

		if (obj.getClass().isAssignableFrom(double.class))
		{
			return (Double)obj;
		} else
		{
			return Double.parseDouble(getString(key));
		}
	}

	/**
	 * Returns the String value to which the specified key is mapped, or the <em>defaultValue</em> if this object contains no mapping for the key.
	 * @param key the key whose associated value is to be returned
	 * @param defaultValue the String value to be returned if this object doesn't contain the specified key.
	 * @return the String value to which the specified key is mapped, or the <em>defaultValue</em> if this object contains no mapping for the key.
	 */
	public String getString(String key, String defaultValue)
	{
		try {
			String s = getString(key);
			return s!=null ? s : defaultValue;
		} catch (GSKeyNotFoundException ex)
		{
			return defaultValue;
		}
	}

	/**
	 * Returns the String value to which the specified key is mapped.
	 * @param key the key whose associated value is to be returned
	 * @throws GSKeyNotFoundException if this object contains no mapping for the specified
	 * @return the String value to which the specified key is mapped.
	 */
	public String getString(String key) throws GSKeyNotFoundException
	{
		if (!map.containsKey(key))
			throw new GSKeyNotFoundException(NO_KEY_EX+key);

		Object obj = map.get(key);
		if (obj==null)
			return null;
		else
			return obj.toString();
	}

	/**
	 * Returns the GSobject value to which the specified key is mapped, or the <em>defaultValue</em> if this object contains no mapping for the key.
	 * @param key the key whose associated value is to be returned
	 * @param defaultValue the GSObject value to be returned if this object doesn't contain the specified key.
	 * @return the GSObject value to which the specified key is mapped, or the <em>defaultValue</em> if this object contains no mapping for the key.
	 */
	public GSObject getObject(String key, GSObject defaultValue)
	{
		try {
			GSObject d = getObject(key);
			return d!=null ? d : defaultValue;
		} catch (GSKeyNotFoundException ex)
		{
			return defaultValue;
		}
	}

	/**
	 * Returns the GSObject value to which the specified key is mapped.
	 * @param key the key whose associated value is to be returned
	 * @throws GSKeyNotFoundException if this object contains no mapping for the specified
	 * @return the GSObject value to which the specified key is mapped.
	 */
	public GSObject getObject(String key) throws GSKeyNotFoundException
	{
		if (!map.containsKey(key))
			throw new GSKeyNotFoundException(NO_KEY_EX+key);
		Object obj = map.get(key);
		if (obj==null)
			return null;
		else
			return (GSObject)obj;
	}

	/**
	 * Returns the GSArray value to which the specified key is mapped, or the <em>defaultValue</em> if this object contains no mapping for the key.
	 * @param key the key whose associated value is to be returned
	 * @param defaultValue the GSArrayvalue to be returned if this object doesn't contain the specified key.
	 * @return the GSArray value to which the specified key is mapped, or the <em>defaultValue</em> if this object contains no mapping for the key.
	 */
	public GSArray getArray(String key, GSArray defaultValue)
	{
		try {
			GSArray  a = getArray(key);
			return a!=null ? a : defaultValue;
		} catch (GSKeyNotFoundException ex)
		{
			return defaultValue;
		}
	}

	/**
	 * Returns the GSArray value to which the specified key is mapped.
	 * @param key the key whose associated value is to be returned
	 * @throws GSKeyNotFoundException if this object contains no mapping for the specified key
	 * @return the GSArray value to which the specified key is mapped.
	 */
	public GSArray getArray(String key) throws GSKeyNotFoundException
	{
		if (!map.containsKey(key))
			throw new GSKeyNotFoundException(NO_KEY_EX+key);
		Object obj = map.get(key);
		if (obj==null)
			return null;
		else
			return (GSArray)obj;
	}

    public Object get(String key, Object defaultValue)
    {
        try {
            Object  a = get(key);
            return a!=null ? a : defaultValue;
        } catch (GSKeyNotFoundException ex)
        {
            return defaultValue;
        }
    }

    public Object get(String key) throws GSKeyNotFoundException
    {
        if (!map.containsKey(key))
            throw new GSKeyNotFoundException(NO_KEY_EX+key);
        Object obj = map.get(key);
        if (obj==null)
            return null;
        else
            return obj;
    }

	/**
	 * Parses parameters from a URL string into this object
	 * @param url the URL string to parse
	 */
	public void parseURL(String url)
	{
		try {
			URL u = new URL(url);
	        parseQueryString(u.getQuery());
	        parseQueryString(u.getRef());
	    } catch (MalformedURLException e) {
	    }
	}

	/**
	 * Parses parameters from a query string into this object.
	 * @param qs the query string to parse
	 */
	public void parseQueryString(String qs)
	{
		if (qs==null) return;
        String array[] = qs.split("&");
        for (String parameter : array)
        {
        	String v[] = parameter.split("=");
        	try {
                String value = "";
                if (v.length > 1)
                    value = URLDecoder.decode(v[1], "UTF8");

        		put(v[0], value);
        	} catch (Exception e){}
        }
	}

	/**
	 * Returns true if this object contains a mapping for the specified key.
	 * @param key key whose presence in this map is to be tested
	 * @return true if this map contains a mapping for the specified key
	 */
	public boolean containsKey(String key)
	{
		return map.containsKey(key);
	}

	/**
	 * Removes the key (and its corresponding value) from this object.
	 * This method does nothing if the key is not in this object.
	 * @param key the key that needs to be removed.
	 */
	public void remove(String key)
	{
		map.remove(key);
	}

	/**
	 * Removes all of the entries from this object. The object will be empty after this call returns.
	 */
	public void clear()
	{
		map.clear();
	}

	/**
	 * Returns a String array containing the keys in this object.
	 * @return a String[] of the keys in this object.
	 */
	public String[] getKeys()
	{
		return map.keySet().toArray(new String[map.keySet().size()]);
	}

	/**
	 * Returns the object's content as a JSON string.
	 * @return the object's content as a JSON string.
	 */
	@Override
	public String toString()
	{
		return toJsonString();
	}

	/**
	 * Returns the object's content as a JSON string.
	 * @return the object's content as a JSON string.
	 */
	public String toJsonString()
	{
		try {
			JSONObject obj = this.toJsonObject();
			return obj.toString();
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Returns a deep copy of the object.
	 * @return a deep copy of the object.
	 */
	public GSObject clone()
	{
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(this);
            out.flush();
            out.close();

            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
            return (GSObject)in.readObject();
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
	}

	/////////////////////////////////////// PRIVATE & PROTECTED ////////////////////////////////////////////
	protected TreeMap<String, Object> getMap()
	{
		return map;
	}

	protected JSONObject toJsonObject() throws JSONException
	{
		JSONObject json= new JSONObject();
		String[] keys = this.getKeys();
		Object val;
		for(String key : keys)
		{
			val = map.get(key);
			if (val != null && val.getClass()==GSObject.class)
			{
				json.put(key, ((GSObject)val).toJsonObject());
			} else if (val != null && val.getClass()==GSArray.class)
			{
				try {
					GSArray array = getArray(key);
					json.put(key, array.toJsonArray());
				} catch (Exception ex)
				{

				}
			} else
			{
				json.put(key, val);
			}
		}
		return json;
	}

	private static void processJsonObject(JSONObject jo, GSObject parentObj) throws Exception
	{
		String key;
		Iterator<?> keys = jo.keys();
		while(keys.hasNext())
		{
			key = keys.next().toString();
			Object value = jo.get(key);
			if (value==null)
			{
				parentObj.put(key, (String)null);
			}
			if (value.getClass().equals(String.class))
			{
				parentObj.put(key, (String)value);
			}
			if (value.getClass().equals(Boolean.class))
			{
				parentObj.put(key, (Boolean)value);
			}
			if (value.getClass().equals(Double.class))
			{
				parentObj.put(key, (Double)value);
			}
			if (value.getClass().equals(Integer.class))
			{
				parentObj.put(key, (Integer)value);
			}
			if (value.getClass().equals(Long.class))
			{
				parentObj.put(key, (Long)value);
			}
			if (value.getClass().equals(JSONObject.class))
			{
				JSONObject subJo = (JSONObject)value;
				GSObject childObj = new GSObject();
				processJsonObject(subJo, childObj);
				parentObj.put(key, childObj);
			}
			if (value.getClass().equals(JSONArray.class))
			{
				JSONArray jsonArray = (JSONArray)value;
				/*
				int size = jsonArray.length();
				GSObject[] childArray = new GSObject[size];
				for(int i=0; i<size; i++)
				{
					childArray[i] = new GSObject(jsonArray.getJSONObject(i));
				}*/
				parentObj.put(key, new GSArray(jsonArray));
			}
		}
	}
}
