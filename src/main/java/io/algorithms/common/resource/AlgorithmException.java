/*
* Copyright 2012 Algorithms.io. All Rights Reserved.
* $Author: rajiv$
* $Date: May 5, 2012$
*/
package io.algorithms.common.resource;

/**
 * Runtime Exception thrown during algorithm execution.
 */
public class AlgorithmException extends Exception {

    private static final long serialVersionUID = 1L;

    public AlgorithmException() {
        super();
    }

    public AlgorithmException(String message, Throwable cause) {
        super(message, cause);
    }

    public AlgorithmException(String message) {
        super(message);
    }

    public AlgorithmException(Throwable cause) {
        super(cause);
    }
}
