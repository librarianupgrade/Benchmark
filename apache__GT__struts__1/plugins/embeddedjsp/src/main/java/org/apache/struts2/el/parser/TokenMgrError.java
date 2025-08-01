/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.struts2.el.parser;

/** Token Manager Error. */
public class TokenMgrError extends Error {

	/*
	* Ordinals for various reasons why an Error of this type can be thrown.
	*/

	/**
	* Lexical error occurred.
	*/
	static final int LEXICAL_ERROR = 0;

	/**
	* An attempt was made to create a second instance of a static token manager.
	*/
	static final int STATIC_LEXER_ERROR = 1;

	/**
	* Tried to change to an invalid lexical state.
	*/
	static final int INVALID_LEXICAL_STATE = 2;

	/**
	* Detected (and bailed out of) an infinite loop in the token manager.
	*/
	static final int LOOP_DETECTED = 3;

	/**
	* Indicates the reason why the exception is thrown. It will have
	* one of the above 4 values.
	*/
	int errorCode;

	/**
	* Replaces unprintable characters by their escaped (or unicode escaped)
	* equivalents in the given string
	*
	* @param str string
	* @return escaped string
	*/
	protected static final String addEscapes(String str) {
		StringBuilder retval = new StringBuilder();
		char ch;
		for (int i = 0; i < str.length(); i++) {
			switch (str.charAt(i)) {
			case 0:
				continue;
			case '\b':
				retval.append("\\b");
				continue;
			case '\t':
				retval.append("\\t");
				continue;
			case '\n':
				retval.append("\\n");
				continue;
			case '\f':
				retval.append("\\f");
				continue;
			case '\r':
				retval.append("\\r");
				continue;
			case '\"':
				retval.append("\\\"");
				continue;
			case '\'':
				retval.append("\\\'");
				continue;
			case '\\':
				retval.append("\\\\");
				continue;
			default:
				if ((ch = str.charAt(i)) < 0x20 || ch > 0x7e) {
					String s = "0000" + Integer.toString(ch, 16);
					retval.append("\\u").append(s.substring(s.length() - 4, s.length()));
				} else {
					retval.append(ch);
				}
			}
		}
		return retval.toString();
	}

	/**
	* Note: You can customize the lexical error message by modifying this method.
	*
	* @param EOFSeen     : indicates if EOF caused the lexical error
	* @param lexState : lexical state in which this error occurred
	* @param errorLine   : line number when the error occurred
	* @param errorColumn : column number when the error occurred
	* @param errorAfter  : prefix that was seen before this error occurred
	* @param curChar     : the offending character
	*
	* @return a detailed message for the Error when it is thrown by the token manager to indicate a lexical error.
	*/
	protected static String LexicalError(boolean EOFSeen, int lexState, int errorLine, int errorColumn,
			String errorAfter, char curChar) {
		return ("Lexical error at line " + errorLine + ", column " + errorColumn + ".  Encountered: "
				+ (EOFSeen ? "<EOF> "
						: ("\"" + addEscapes(String.valueOf(curChar)) + "\"") + " (" + (int) curChar + "), ")
				+ "after : \"" + addEscapes(errorAfter) + "\"");
	}

	/**
	* You can also modify the body of this method to customize your error messages.
	* For example, cases like LOOP_DETECTED and INVALID_LEXICAL_STATE are not
	* of end-users concern, so you can return something like :
	*
	*     "Internal Error : Please file a bug report .... "
	*
	* from this method for such cases in the release version of your parser.
	*
	* @return the message
	*/
	public String getMessage() {
		return super.getMessage();
	}

	/*
	* Constructors of various flavors follow.
	*/

	/** No arg constructor. */
	public TokenMgrError() {
	}

	/**
	* Constructor with message and reason.
	*
	* @param message the error message
	* @param reason the reason
	*/
	public TokenMgrError(String message, int reason) {
		super(message);
		errorCode = reason;
	}

	/**
	* Full Constructor.
	*
	* @param EOFSeen indicates if EOF caused the lexical error
	* @param lexState lexical state in which this error occurred
	* @param errorLine line number when the error occurred
	* @param errorColumn column number when the error occurred
	* @param errorAfter prefix that was seen before this error occurred
	* @param curChar the offending character
	* @param reason the reason
	*/
	public TokenMgrError(boolean EOFSeen, int lexState, int errorLine, int errorColumn, String errorAfter, char curChar,
			int reason) {
		this(LexicalError(EOFSeen, lexState, errorLine, errorColumn, errorAfter, curChar), reason);
	}
}
/* JavaCC - OriginalChecksum=8048f4b229a762baa426e8e8436dbe9e (do not edit this line) */
