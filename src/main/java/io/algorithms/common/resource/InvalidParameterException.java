/*
* Copyright 2012 Algorithms.io. All Rights Reserved.
* $Author: rajiv$
* $Date: May 5, 2012$
*/
package io.algorithms.common.resource;

/**
 * Thrown when the parameters passed to an algorithm are invalid.
 */
public class InvalidParameterException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidParameterException() {
        super();
    }

    public InvalidParameterException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidParameterException(String message) {
        super(message);
    }

    public InvalidParameterException(Throwable cause) {
        super(cause);
    }

}
