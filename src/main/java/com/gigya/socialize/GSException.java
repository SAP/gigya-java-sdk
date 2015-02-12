/*
 * Copyright (C) 2011 Gigya, Inc.
 * Version java_3.0
 */
package main.java.com.gigya.socialize;

/**
 * General Gigya exception.
 * @author Raviv Pavel
 */
@SuppressWarnings("serial")
public class GSException extends Exception {

	public GSException(String msg)
	{
		super(msg);
	}
}
