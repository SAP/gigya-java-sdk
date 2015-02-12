/*
 * Copyright (C) 2011 Gigya, Inc.
 * Version java_3.0
 */

package main.java.com.gigya.socialize;

/**
 * Interface for listening to server responses. Passed to GSRequest.send.
  */
public abstract interface GSResponseListener {

	/**
	 * This method will be invoked after the Gigya server completes to process your request. The method receives Gigya's response and should handle it.
	 * @param method the name of the Gigya API method that was requested.
	 * @param response Gigya's response.
	 * @param context the context object passed by the application as a parameter to the API method, or null if no context object has been passed.
	 */
	public abstract void onGSResponse(String method, GSResponse response, Object context);
}
