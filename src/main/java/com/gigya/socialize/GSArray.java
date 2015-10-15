package com.gigya.socialize;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Used for passing arrays. For example, when issuing requests or receiving response data.  <br/>
 * The value may be one of the following types: <br/>
 * <ul>
 * <li> String
 * <li> boolean
 * <li> int
 * <li> long
 * <li> double
 * <li> GSObject
 * <li> GSArray
 * </ul>
 */
@SuppressWarnings("serial")
public class GSArray implements Serializable, Iterable<Object> {
    private ArrayList<Object> array = new ArrayList<Object>();
    private static final String NO_INDEX_EX = "GSArray does not contain a value at index ";

    /**
     * Empty constructor
     */
    public GSArray() {
    }

    /**
     * Constructor from json string
     *
     * @param json json string
     * @throws Exception
     */
    public GSArray(String json) throws Exception {
        this(new JSONArray(json));
    }

    /**
     * Constructor from JSONArray
     *
     * @param jsonArray
     * @throws Exception
     */
    protected GSArray(JSONArray jsonArray) throws Exception {
        for (int i = 0; i < jsonArray.length(); i++) {
            if (jsonArray.isNull(i)) {
                array.add(null);
            } else {
                Object value = jsonArray.get(i);
                if (value.getClass().equals(JSONObject.class)) {
                    GSObject o = new GSObject((JSONObject) value);
                    array.add(o);
                } else if (value.getClass().equals(JSONArray.class)) {
                    JSONArray a = (JSONArray) value;
                    array.add(new GSArray(a));
                } else {
                    array.add(value);
                }
            }
        }
    }

    /**
     * return the array's length
     *
     * @return
     */
    public int length() {
        return this.array.size();
    }

    public void add(String val) {
        array.add(val);
    }

    public void add(int val) {
        array.add(val);
    }

    public void add(long val) {
        array.add(val);
    }

    public void add(double val) {
        array.add(val);
    }

    public void add(boolean val) {
        array.add(val);
    }

    public void add(GSObject val) {
        array.add(val);
    }

    public void add(GSArray val) {
        array.add(val);
    }

    public void add(Object val) {
        array.add(val);
    }

    /**
     * Get string value at index
     *
     * @param index
     * @return
     */
    public String getString(int index) {
        Object obj = array.get(index);
        if (obj == null)
            return null;
        else
            return obj.toString();
    }

    /**
     * Get bool value at index
     *
     * @param index
     * @return
     * @throws NullPointerException      if value is null
     * @throws IndexOutOfBoundsException
     */
    public boolean getBool(int index) {
        Object obj = array.get(index);
        if (obj == null)
            throw new NullPointerException(NO_INDEX_EX + index);

        if (obj.getClass().isAssignableFrom(Boolean.class)) {
            return (Boolean) obj;
        } else {
            return obj.toString().toLowerCase().equals("true") || obj.toString().equals("1");
        }
    }

    /**
     * Get int value at index
     *
     * @param index
     * @return
     * @throws NullPointerException      if value is null
     * @throws IndexOutOfBoundsException
     */
    public int getInt(int index) {
        Object obj = array.get(index);
        if (obj == null)
            throw new NullPointerException(NO_INDEX_EX + index);

        if (obj.getClass().isAssignableFrom(Integer.class)) {
            return (Integer) obj;
        } else {
            return Integer.parseInt(getString(index));
        }
    }

    /**
     * Get long value at index
     *
     * @param index
     * @return
     * @throws NullPointerException      if value is null
     * @throws IndexOutOfBoundsException
     */
    public long getLong(int index) {
        Object obj = array.get(index);
        if (obj == null)
            throw new NullPointerException(NO_INDEX_EX + index);

        if (obj.getClass().isAssignableFrom(Long.class)) {
            return (Long) obj;
        } else {
            return Long.parseLong(getString(index));
        }
    }

    /**
     * Get double value at index
     *
     * @param index
     * @return
     * @throws NullPointerException      if value is null
     * @throws IndexOutOfBoundsException
     */
    public double getDouble(int index) {
        Object obj = array.get(index);
        if (obj == null)
            throw new NullPointerException(NO_INDEX_EX + index);

        if (obj.getClass().isAssignableFrom(Double.class)) {
            return (Double) obj;
        } else {
            return Double.parseDouble(getString(index));
        }
    }

    /**
     * Get GSObject value at index
     *
     * @param index
     * @return
     * @throws IndexOutOfBoundsException
     */
    public GSObject getObject(int index) {
        Object obj = array.get(index);
        if (obj == null)
            return null;
        else
            return (GSObject) obj;
    }

    /**
     * Get GSArray value at index
     *
     * @param index
     * @throws IndexOutOfBoundsException
     */
    public GSArray getArray(int index) {
        Object obj = array.get(index);
        if (obj == null)
            return null;
        else
            return (GSArray) obj;
    }

    @Override
    public String toString() {
        return toJsonString();
    }

    public String toJsonString() {
        try {
            JSONArray a = this.toJsonArray();
            return a.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Iterator<Object> iterator() {
        return array.iterator();
    }

    protected JSONArray toJsonArray() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (Object obj : array) {
            if (obj == null) {
                jsonArray.put(JSONObject.NULL);
                continue;
            }

            Class objClass = obj.getClass();
            if (objClass == GSObject.class) {
                jsonArray.put(((GSObject) obj).toJsonObject());
            } else if (objClass == GSArray.class) {
                try {
                    GSArray arrayObj = (GSArray) obj;
                    jsonArray.put(arrayObj.toJsonArray());
                } catch (Exception ex) {

                }
            } else {
                jsonArray.put(obj);
            }
        }
        return jsonArray;
    }

    protected ArrayList<Object> getUnderlingArray() {
        return array;
    }
}
