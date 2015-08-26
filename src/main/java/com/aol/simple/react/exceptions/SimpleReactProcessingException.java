package com.aol.simple.react.exceptions;

public class SimpleReactProcessingException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public SimpleReactProcessingException() {
		super();
		
	}

	@Override
	public synchronized Throwable fillInStackTrace() {
		return this;
	}

	public SimpleReactProcessingException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		
	}

	public SimpleReactProcessingException(String message, Throwable cause) {
		super(message, cause);
		
	}

	public SimpleReactProcessingException(String message) {
		super(message);
		
	}

	public SimpleReactProcessingException(Throwable cause) {
		super(cause);
		
	}
}
