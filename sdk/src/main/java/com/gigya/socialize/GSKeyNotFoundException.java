package com.gigya.socialize;

/**
 * Thrown when attempting to fetch a key that does not exist.
 */
@SuppressWarnings("serial")
public class GSKeyNotFoundException extends GSException {

    public GSKeyNotFoundException(String msg) {
        super(msg);
    }
}
