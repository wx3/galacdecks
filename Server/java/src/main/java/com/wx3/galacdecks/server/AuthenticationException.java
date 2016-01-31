/**
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 Kevin Lin
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 */
/**
 * 
 */
package com.wx3.galacdecks.server;

/**
 * Thrown in the event of a problem authenticating the client.
 * 
 * @author Kevin
 *
 */
public class AuthenticationException extends RuntimeException {
	
	private static final long serialVersionUID = 6004L;
	
	public static final String NO_TOKEN = "NO_TOKEN";
	public static final String BAD_TOKEN = "BAD_TOKEN";
	public static final String MISSING_GAME = "MISSING_GAME";
	public static final String UNKNOWN = "UNKNOWN_ERROR";
	
	private String code;
	
	public AuthenticationException(String error) {
		super("Authentication Exception: " + error);
		this.code = error;
	}
	
	public AuthenticationException(String error, Throwable cause) {
		super("Authentication Exception: " + error, cause);
		this.code = error;
	}
	
	public String getCode() {
		return code;
	}
}
