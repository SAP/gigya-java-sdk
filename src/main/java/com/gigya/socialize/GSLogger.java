/*
 * Copyright (C) 2011 Gigya, Inc.
 * Version java_3.0
 */

package com.gigya.socialize;

import java.io.PrintWriter;
import java.io.StringWriter;

public class GSLogger {
    private StringBuilder sb = new StringBuilder();

    public void write(Object data) {
        if (data == null) return;
        write(null, data.toString());
    }

    public void write(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        write(sw);
    }

    public void write(String key, Object data) {
        if (key != null)
            sb.append(key + ": ");
        if (data != null) {
            String s = data.toString();

            if (s.length() > 10000)
                s = String.format("%s.. (value too long)", s.substring(0, 10000));

            sb.append(s + "\n");
        }
    }

    public void writeFormat(String format, Object... args) {
        write(String.format(format, args));
    }

    @Override
    public String toString() {
        return sb.toString();
    }

}
