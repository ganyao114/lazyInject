package com.trend.lazyinject.lib.exception;

public class ComponentBuildException extends RuntimeException {
    public ComponentBuildException(String message) {
        super(message);
    }

    public ComponentBuildException(String message, Throwable cause) {
        super(message, cause);
    }
}
