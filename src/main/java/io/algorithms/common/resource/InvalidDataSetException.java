/*
* Copyright 2012 Algorithms.io. All Rights Reserved.
* $Author: rajiv$
* $Date: May 5, 2012$
*/
package io.algorithms.common.resource;

/**
 * Thrown when the parameters passed to an algorithm are invalid.
 */
public class InvalidDataSetException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidDataSetException() {
        super();
    }

    public InvalidDataSetException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidDataSetException(String message) {
        super(message);
    }

    public InvalidDataSetException(Throwable cause) {
        super(cause);
    }

}
